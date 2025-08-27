import * as fs from 'fs';
import * as path from 'path';
import {
  Finding,
  FindingCategory,
  Severity,
  EffortLevel,
  ImpactLevel,
} from '../analysis-aggregator';

export class PerformanceAnalyzer {
  private findings: Finding[] = [];

  async analyzeService(
    servicePath: string,
    serviceName: string
  ): Promise<Finding[]> {
    this.findings = [];

    console.log(`⚡ Analyzing performance for service: ${serviceName}`);

    await Promise.all([
      this.analyzeDatabaseQueries(servicePath, serviceName),
      this.analyzeMemoryUsage(servicePath, serviceName),
      this.analyzeApiPerformance(servicePath, serviceName),
      this.analyzeCachingStrategies(servicePath, serviceName),
    ]);

    return this.findings;
  }

  private async analyzeDatabaseQueries(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for N+1 query problems
      if (content.includes('@OneToMany') || content.includes('@ManyToOne')) {
        if (
          !content.includes('fetch = FetchType.LAZY') &&
          !content.includes('@BatchSize')
        ) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.HIGH,
            location: { file },
            description: `Potential N+1 query problem with eager loading`,
            recommendation: `Use LAZY loading with @BatchSize or JOIN FETCH queries`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }

      // Check for missing database indexes
      if (content.includes('@Query') && content.includes('WHERE')) {
        const queryMatch = content.match(/@Query\s*\(\s*["']([^"']+)["']/);
        if (queryMatch) {
          const query = queryMatch[1];
          if (query.includes('WHERE') && !query.includes('INDEX')) {
            this.addFinding({
              category: FindingCategory.PERFORMANCE,
              severity: Severity.MEDIUM,
              location: { file },
              description: `Custom query may need database index optimization`,
              recommendation: `Review query execution plan and add appropriate indexes`,
              estimatedEffort: EffortLevel.MEDIUM,
              businessImpact: ImpactLevel.MEDIUM,
            });
          }
        }
      }

      // Check for inefficient pagination
      if (content.includes('findAll()') && !content.includes('Pageable')) {
        this.addFinding({
          category: FindingCategory.PERFORMANCE,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Using findAll() without pagination`,
          recommendation: `Implement pagination using Pageable parameter`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for transaction boundaries
      if (content.includes('@Service') && !content.includes('@Transactional')) {
        if (content.includes('save(') || content.includes('delete(')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Service methods without proper transaction boundaries`,
            recommendation: `Add @Transactional annotation to service methods`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }
    }
  }

  private async analyzeMemoryUsage(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for memory leaks in collections
      if (
        content.includes('mutableListOf()') ||
        content.includes('mutableMapOf()')
      ) {
        if (!content.includes('clear()') && content.includes('class ')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Mutable collections that may cause memory leaks`,
            recommendation: `Ensure collections are properly cleared or use immutable collections`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }

      // Check for inefficient string operations
      if (content.includes('String') && content.includes('+')) {
        const stringConcatCount = (content.match(/\+\s*["']/g) || []).length;
        if (stringConcatCount > 3) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.LOW,
            location: { file },
            description: `Inefficient string concatenation detected`,
            recommendation: `Use StringBuilder or string templates for multiple concatenations`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.LOW,
          });
        }
      }

      // Check for large object creation in loops
      if (content.includes('for (') || content.includes('forEach')) {
        if (content.includes('new ') || content.includes('mutableListOf')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Object creation inside loops may impact performance`,
            recommendation: `Move object creation outside loops or use object pooling`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }
    }
  }

  private async analyzeApiPerformance(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const controllerFiles = this.getAllKotlinFiles(servicePath).filter(
      f => f.includes('controller') || f.includes('Controller')
    );

    for (const file of controllerFiles) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for missing async processing
      if (content.includes('@PostMapping') || content.includes('@PutMapping')) {
        if (
          !content.includes('suspend') &&
          !content.includes('CompletableFuture')
        ) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.LOW,
            location: { file },
            description: `Synchronous processing in POST/PUT endpoints`,
            recommendation: `Consider async processing for long-running operations`,
            estimatedEffort: EffortLevel.HIGH,
            businessImpact: ImpactLevel.LOW,
          });
        }
      }

      // Check for missing response compression
      if (!content.includes('@Compress') && !content.includes('gzip')) {
        this.addFinding({
          category: FindingCategory.PERFORMANCE,
          severity: Severity.LOW,
          location: { file },
          description: `API responses not compressed`,
          recommendation: `Enable GZIP compression for API responses`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.LOW,
        });
      }

      // Check for excessive data transfer
      if (content.includes('findAll()') && content.includes('ResponseEntity')) {
        this.addFinding({
          category: FindingCategory.PERFORMANCE,
          severity: Severity.MEDIUM,
          location: { file },
          description: `API endpoint returning all data without filtering`,
          recommendation: `Implement filtering, pagination, and field selection`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }
    }
  }

  private async analyzeCachingStrategies(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllKotlinFiles(servicePath);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for missing caching on expensive operations
      if (content.includes('@Service') && content.includes('findBy')) {
        if (!content.includes('@Cacheable') && !content.includes('@Cache')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.MEDIUM,
            location: { file },
            description: `Database queries without caching`,
            recommendation: `Add @Cacheable annotation to frequently accessed data`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }

      // Check for cache invalidation
      if (
        content.includes('@Cacheable') &&
        (content.includes('save(') || content.includes('delete('))
      ) {
        if (!content.includes('@CacheEvict')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.HIGH,
            location: { file },
            description: `Cached data without proper invalidation`,
            recommendation: `Add @CacheEvict annotation to update/delete operations`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }
    }

    // Check application configuration for caching
    const configFiles = [
      path.join(servicePath, 'src/main/resources/application.yml'),
      path.join(servicePath, 'src/main/resources/application.yaml'),
      path.join(servicePath, 'src/main/resources/application.properties'),
    ];

    for (const configFile of configFiles) {
      if (fs.existsSync(configFile)) {
        const content = fs.readFileSync(configFile, 'utf8');

        if (!content.includes('cache') && !content.includes('redis')) {
          this.addFinding({
            category: FindingCategory.PERFORMANCE,
            severity: Severity.LOW,
            location: { file: configFile },
            description: `No caching configuration found`,
            recommendation: `Configure Redis or in-memory caching for better performance`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      }
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
      id: `perf-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      agentId: 'performance-analyzer',
      timestamp: new Date(),
    });
  }
}
