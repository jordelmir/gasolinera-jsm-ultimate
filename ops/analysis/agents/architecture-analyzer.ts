import * as fs from 'fs';
import * as path from 'path';
import {
  Finding,
  FindingCategory,
  Severity,
  EffortLevel,
  ImpactLevel,
  CodeLocation,
} from '../analysis-aggregator';

export class ArchitectureAnalyzer {
  private findings: Finding[] = [];

  async analyzeService(
    servicePath: string,
    serviceName: string
  ): Promise<Finding[]> {
    this.findings = [];

    console.log(`🏗️  Analyzing architecture for service: ${serviceName}`);

    await Promise.all([
      this.analyzeSpringBootPatterns(servicePath, serviceName),
      this.analyzeDependencyInjection(servicePath, serviceName),
      this.analyzeApiDesign(servicePath, serviceName),
      this.analyzeLayerSeparation(servicePath, serviceName),
      this.analyzeCircularDependencies(servicePath, serviceName),
    ]);

    return this.findings;
  }

  private async analyzeSpringBootPatterns(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const srcPath = path.join(servicePath, 'src/main/kotlin');
    if (!fs.existsSync(srcPath)) return;

    const files = this.getAllKotlinFiles(srcPath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for proper controller annotations
      if (file.includes('controller') || file.includes('Controller')) {
        if (
          !content.includes('@RestController') &&
          !content.includes('@Controller')
        ) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Controller class missing proper Spring annotations`,
            recommendation: `Add @RestController annotation to controller classes`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }

      // Check for service layer patterns
      if (file.includes('service') || file.includes('Service')) {
        if (!content.includes('@Service') && !content.includes('@Component')) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Service class missing @Service annotation`,
            recommendation: `Add @Service annotation for proper Spring component scanning`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }

      // Check for repository patterns
      if (file.includes('repository') || file.includes('Repository')) {
        if (
          !content.includes('@Repository') &&
          !content.includes('JpaRepository')
        ) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.HIGH,
            location: { file },
            description: `Repository not following Spring Data patterns`,
            recommendation: `Use @Repository annotation or extend JpaRepository interface`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }
    }
  }

  private async analyzeDependencyInjection(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const srcPath = path.join(servicePath, 'src/main/kotlin');
    if (!fs.existsSync(srcPath)) return;

    const files = this.getAllKotlinFiles(srcPath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for field injection (anti-pattern)
      if (content.includes('@Autowired') && content.includes('var ')) {
        this.addFinding({
          category: FindingCategory.ARCHITECTURE,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Field injection detected - prefer constructor injection`,
          recommendation: `Use constructor injection instead of @Autowired field injection`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for missing dependency validation
      if (
        content.includes('class ') &&
        content.includes('(') &&
        !content.includes('require')
      ) {
        const constructorMatch = content.match(/class\s+\w+\s*\([^)]*\)/);
        if (constructorMatch && constructorMatch[0].includes(':')) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.LOW,
            location: { file },
            description: `Constructor dependencies not validated`,
            recommendation: `Add null checks or use require() for constructor parameters`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.LOW,
          });
        }
      }
    }
  }

  private async analyzeApiDesign(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const srcPath = path.join(servicePath, 'src/main/kotlin');
    if (!fs.existsSync(srcPath)) return;

    const controllerFiles = this.getAllKotlinFiles(srcPath).filter(
      f => f.includes('controller') || f.includes('Controller')
    );

    for (const file of controllerFiles) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for proper HTTP method usage
      const httpMethods = [
        '@GetMapping',
        '@PostMapping',
        '@PutMapping',
        '@DeleteMapping',
        '@PatchMapping',
      ];
      const hasHttpMethods = httpMethods.some(method =>
        content.includes(method)
      );

      if (!hasHttpMethods && content.includes('@RequestMapping')) {
        this.addFinding({
          category: FindingCategory.ARCHITECTURE,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Using generic @RequestMapping instead of specific HTTP method annotations`,
          recommendation: `Use specific annotations like @GetMapping, @PostMapping instead of @RequestMapping`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for proper response entity usage
      if (
        content.includes('fun ') &&
        !content.includes('ResponseEntity') &&
        content.includes('Mapping')
      ) {
        this.addFinding({
          category: FindingCategory.ARCHITECTURE,
          severity: Severity.LOW,
          location: { file },
          description: `Controller methods not returning ResponseEntity`,
          recommendation: `Use ResponseEntity for better HTTP response control`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.LOW,
        });
      }

      // Check for input validation
      if (content.includes('@PostMapping') || content.includes('@PutMapping')) {
        if (!content.includes('@Valid') && !content.includes('@Validated')) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.HIGH,
            location: { file },
            description: `Missing input validation on POST/PUT endpoints`,
            recommendation: `Add @Valid annotation to request body parameters`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }
    }
  }

  private async analyzeLayerSeparation(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const srcPath = path.join(servicePath, 'src/main/kotlin');
    if (!fs.existsSync(srcPath)) return;

    const packageStructure = this.analyzePackageStructure(srcPath);

    // Check for proper layer separation
    const expectedLayers = [
      'controller',
      'service',
      'repository',
      'model',
      'dto',
    ];
    const missingLayers = expectedLayers.filter(
      layer => !packageStructure.includes(layer)
    );

    if (missingLayers.length > 0) {
      this.addFinding({
        category: FindingCategory.ARCHITECTURE,
        severity: Severity.MEDIUM,
        location: { file: srcPath },
        description: `Missing standard architectural layers: ${missingLayers.join(', ')}`,
        recommendation: `Organize code into standard layers: controller, service, repository, model, dto`,
        estimatedEffort: EffortLevel.HIGH,
        businessImpact: ImpactLevel.MEDIUM,
      });
    }

    // Check for cross-layer dependencies
    const files = this.getAllKotlinFiles(srcPath);
    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      if (file.includes('controller')) {
        if (content.includes('repository') && !content.includes('service')) {
          this.addFinding({
            category: FindingCategory.ARCHITECTURE,
            severity: Severity.HIGH,
            location: { file },
            description: `Controller directly accessing repository layer`,
            recommendation: `Controllers should only access service layer, not repositories directly`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }
    }
  }

  private async analyzeCircularDependencies(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const buildFile = path.join(servicePath, 'build.gradle.kts');
    if (!fs.existsSync(buildFile)) return;

    const content = fs.readFileSync(buildFile, 'utf8');

    // Simple check for potential circular dependencies in build file
    const dependencies = this.extractDependencies(content);
    const internalDeps = dependencies.filter(dep =>
      dep.includes('gasolinerajsm')
    );

    if (internalDeps.length > 3) {
      this.addFinding({
        category: FindingCategory.ARCHITECTURE,
        severity: Severity.MEDIUM,
        location: { file: buildFile },
        description: `High number of internal service dependencies (${internalDeps.length})`,
        recommendation: `Review service boundaries and reduce coupling between services`,
        estimatedEffort: EffortLevel.HIGH,
        businessImpact: ImpactLevel.MEDIUM,
      });
    }
  }

  private getAllKotlinFiles(dir: string): string[] {
    const files: string[] = [];

    const traverse = (currentDir: string) => {
      const items = fs.readdirSync(currentDir, { withFileTypes: true });

      for (const item of items) {
        const fullPath = path.join(currentDir, item.name);

        if (item.isDirectory()) {
          traverse(fullPath);
        } else if (item.name.endsWith('.kt') || item.name.endsWith('.kts')) {
          files.push(fullPath);
        }
      }
    };

    if (fs.existsSync(dir)) {
      traverse(dir);
    }

    return files;
  }

  private analyzePackageStructure(srcPath: string): string[] {
    const packages: string[] = [];

    const traverse = (currentDir: string) => {
      const items = fs.readdirSync(currentDir, { withFileTypes: true });

      for (const item of items) {
        if (item.isDirectory()) {
          packages.push(item.name);
          traverse(path.join(currentDir, item.name));
        }
      }
    };

    if (fs.existsSync(srcPath)) {
      traverse(srcPath);
    }

    return packages;
  }

  private extractDependencies(buildContent: string): string[] {
    const depRegex =
      /(?:implementation|api|testImplementation)\s*\(\s*["']([^"']+)["']\s*\)/g;
    const dependencies: string[] = [];
    let match;

    while ((match = depRegex.exec(buildContent)) !== null) {
      dependencies.push(match[1]);
    }

    return dependencies;
  }

  private addFinding(
    finding: Omit<Finding, 'id' | 'agentId' | 'timestamp'>
  ): void {
    this.findings.push({
      ...finding,
      id: `arch-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      agentId: 'architecture-analyzer',
      timestamp: new Date(),
    });
  }
}
