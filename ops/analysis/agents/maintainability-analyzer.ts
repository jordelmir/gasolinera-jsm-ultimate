import * as fs from 'fs';
import * as path from 'path';
import {
  Finding,
  FindingCategory,
  Severity,
  EffortLevel,
  ImpactLevel,
} from '../analysis-aggregator';

export class MaintainabilityAnalyzer {
  private findings: Finding[] = [];

  async analyzeService(
    servicePath: string,
    serviceName: string
  ): Promise<Finding[]> {
    this.findings = [];

    console.log(`🔧 Analyzing maintainability for service: ${serviceName}`);

    await Promise.all([
      this.analyzeCodeStyle(servicePath, serviceName),
      this.analyzeTypeSafety(servicePath, serviceName),
      this.analyzeDocumentation(servicePath, serviceName),
      this.analyzeTestCoverage(servicePath, serviceName),
    ]);

    return this.findings;
  }

  private async analyzeCodeStyle(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');
      const lines = content.split('\n');

      // Check for long methods
      let currentMethodLines = 0;
      let inMethod = false;

      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();

        if (line.includes('fun ') && line.includes('(')) {
          inMethod = true;
          currentMethodLines = 1;
        } else if (inMethod) {
          currentMethodLines++;
          if (line === '}' && currentMethodLines > 50) {
            this.addFinding({
              category: FindingCategory.MAINTAINABILITY,
              severity: Severity.MEDIUM,
              location: { file, line: i - currentMethodLines + 1 },
              description: `Method too long (${currentMethodLines} lines)`,
              recommendation: `Break down method into smaller, focused functions`,
              estimatedEffort: EffortLevel.MEDIUM,
              businessImpact: ImpactLevel.MEDIUM,
            });
            inMethod = false;
          } else if (line === '}') {
            inMethod = false;
          }
        }
      }

      // Check for magic numbers
      const magicNumberRegex = /\b(?!0|1|2|10|100|1000)\d{2,}\b/g;
      const magicNumbers = content.match(magicNumberRegex);
      if (magicNumbers && magicNumbers.length > 3) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.LOW,
          location: { file },
          description: `Multiple magic numbers found (${magicNumbers.length})`,
          recommendation: `Extract magic numbers to named constants`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.LOW,
        });
      }

      // Check for deep nesting
      let maxNesting = 0;
      let currentNesting = 0;

      for (const line of lines) {
        const openBraces = (line.match(/{/g) || []).length;
        const closeBraces = (line.match(/}/g) || []).length;
        currentNesting += openBraces - closeBraces;
        maxNesting = Math.max(maxNesting, currentNesting);
      }

      if (maxNesting > 4) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Deep nesting detected (${maxNesting} levels)`,
          recommendation: `Reduce nesting by extracting methods or using early returns`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for large classes
      const classLines = lines.filter(
        line =>
          !line.trim().startsWith('//') &&
          !line.trim().startsWith('/*') &&
          line.trim().length > 0
      ).length;

      if (classLines > 300) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Large class detected (${classLines} lines)`,
          recommendation: `Consider breaking class into smaller, focused classes`,
          estimatedEffort: EffortLevel.HIGH,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }
    }
  }

  private async analyzeTypeSafety(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for Any type usage
      if (content.includes(': Any') || content.includes('<Any>')) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Using Any type reduces type safety`,
          recommendation: `Use specific types instead of Any for better type safety`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for nullable types without proper handling
      const nullableRegex = /:\s*\w+\?/g;
      const nullableTypes = content.match(nullableRegex) || [];
      const nullChecks =
        (content.match(/\?\./g) || []).length +
        (content.match(/!!/g) || []).length;

      if (nullableTypes.length > nullChecks) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Nullable types without proper null handling`,
          recommendation: `Use safe calls (?.) or null checks for nullable types`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for force unwrapping
      const forceUnwraps = (content.match(/!!/g) || []).length;
      if (forceUnwraps > 2) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.HIGH,
          location: { file },
          description: `Excessive use of force unwrapping (!! operator)`,
          recommendation: `Use safe calls or proper null checks instead of force unwrapping`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.HIGH,
        });
      }
    }
  }

  private async analyzeDocumentation(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');
      const lines = content.split('\n');

      // Check for missing class documentation
      if (content.includes('class ') && !content.includes('/**')) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.LOW,
          location: { file },
          description: `Class missing KDoc documentation`,
          recommendation: `Add KDoc comments to document class purpose and usage`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.LOW,
        });
      }

      // Check for public methods without documentation
      let publicMethodsCount = 0;
      let documentedMethodsCount = 0;

      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();
        const prevLine = i > 0 ? lines[i - 1].trim() : '';

        if (line.includes('fun ') && !line.includes('private')) {
          publicMethodsCount++;
          if (prevLine.startsWith('/**') || prevLine.startsWith('/*')) {
            documentedMethodsCount++;
          }
        }
      }

      if (
        publicMethodsCount > 0 &&
        documentedMethodsCount / publicMethodsCount < 0.5
      ) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.LOW,
          location: { file },
          description: `Low documentation coverage for public methods (${Math.round((documentedMethodsCount / publicMethodsCount) * 100)}%)`,
          recommendation: `Add KDoc comments to public methods`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.LOW,
        });
      }
    }

    // Check for README files
    const readmeFiles = ['README.md', 'readme.md', 'README.txt'];
    const hasReadme = readmeFiles.some(readme =>
      fs.existsSync(path.join(servicePath, readme))
    );

    if (!hasReadme) {
      this.addFinding({
        category: FindingCategory.MAINTAINABILITY,
        severity: Severity.LOW,
        location: { file: servicePath },
        description: `Service missing README documentation`,
        recommendation: `Create README.md with service description, setup, and usage instructions`,
        estimatedEffort: EffortLevel.LOW,
        businessImpact: ImpactLevel.LOW,
      });
    }
  }

  private async analyzeTestCoverage(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const srcFiles = this.getAllKotlinFiles(path.join(servicePath, 'src/main'));
    const testFiles = this.getAllKotlinFiles(
      path.join(servicePath, 'src/test')
    );

    if (srcFiles.length === 0) return;

    const testCoverageRatio = testFiles.length / srcFiles.length;

    if (testCoverageRatio < 0.5) {
      this.addFinding({
        category: FindingCategory.MAINTAINABILITY,
        severity: Severity.MEDIUM,
        location: { file: servicePath },
        description: `Low test coverage ratio (${Math.round(testCoverageRatio * 100)}%)`,
        recommendation: `Increase test coverage by adding unit tests for main classes`,
        estimatedEffort: EffortLevel.HIGH,
        businessImpact: ImpactLevel.MEDIUM,
      });
    }

    // Check for test quality
    for (const testFile of testFiles) {
      const content = fs.readFileSync(testFile, 'utf8');

      // Check for proper test structure
      if (
        !content.includes('@Test') &&
        !content.includes('@ParameterizedTest')
      ) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.MEDIUM,
          location: { file: testFile },
          description: `Test file without proper test annotations`,
          recommendation: `Use @Test or @ParameterizedTest annotations for test methods`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for assertions
      const assertionCount = (content.match(/assert/gi) || []).length;
      const testMethodCount = (content.match(/@Test/g) || []).length;

      if (testMethodCount > 0 && assertionCount / testMethodCount < 1) {
        this.addFinding({
          category: FindingCategory.MAINTAINABILITY,
          severity: Severity.HIGH,
          location: { file: testFile },
          description: `Test methods with insufficient assertions`,
          recommendation: `Add proper assertions to verify test expectations`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.HIGH,
        });
      }
    }

    // Check for integration tests
    const hasIntegrationTests = testFiles.some(
      file => file.includes('Integration') || file.includes('IT.kt')
    );

    if (
      !hasIntegrationTests &&
      srcFiles.some(file => file.includes('Controller'))
    ) {
      this.addFinding({
        category: FindingCategory.MAINTAINABILITY,
        severity: Severity.MEDIUM,
        location: { file: servicePath },
        description: `Service with controllers missing integration tests`,
        recommendation: `Add integration tests to verify API endpoints`,
        estimatedEffort: EffortLevel.HIGH,
        businessImpact: ImpactLevel.MEDIUM,
      });
    }
  }

  private getAllKotlinFiles(dir: string): string[] {
    const files: string[] = [];

    const traverse = (currentDir: string) => {
      if (!fs.existsSync(currentDir)) return;

      const items = fs.readdirSync(currentDir, { withFileTypes: true });

      for (const item of items) {
        const fullPath = path.join(currentDir, item.name);

        if (
          item.isDirectory() &&
          !item.name.includes('build') &&
          !item.name.includes('node_modules')
        ) {
          traverse(fullPath);
        } else if (item.name.endsWith('.kt') || item.name.endsWith('.kts')) {
          files.push(fullPath);
        }
      }
    };

    traverse(dir);
    return files;
  }

  private addFinding(
    finding: Omit<Finding, 'id' | 'agentId' | 'timestamp'>
  ): void {
    this.findings.push({
      ...finding,
      id: `maint-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      agentId: 'maintainability-analyzer',
      timestamp: new Date(),
    });
  }
}
