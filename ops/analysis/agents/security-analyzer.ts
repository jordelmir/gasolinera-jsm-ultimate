import * as fs from 'fs';
import * as path from 'path';
import {
  Finding,
  FindingCategory,
  Severity,
  EffortLevel,
  ImpactLevel,
} from '../analysis-aggregator';

export class SecurityAnalyzer {
  private findings: Finding[] = [];

  async analyzeService(
    servicePath: string,
    serviceName: string
  ): Promise<Finding[]> {
    this.findings = [];

    console.log(`🔒 Analyzing security for service: ${serviceName}`);

    await Promise.all([
      this.analyzeAuthentication(servicePath, serviceName),
      this.analyzeAuthorization(servicePath, serviceName),
      this.analyzeInputValidation(servicePath, serviceName),
      this.analyzeSensitiveDataExposure(servicePath, serviceName),
      this.analyzeDependencyVulnerabilities(servicePath, serviceName),
    ]);

    return this.findings;
  }

  private async analyzeAuthentication(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllFiles(servicePath, [
      '.kt',
      '.kts',
      '.yml',
      '.yaml',
      '.properties',
    ]);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for JWT implementation
      if (content.includes('JWT') || content.includes('jwt')) {
        if (!content.includes('verify') && !content.includes('validate')) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.HIGH,
            location: { file },
            description: `JWT tokens used without proper validation`,
            recommendation: `Implement JWT signature verification and expiration checks`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }

      // Check for hardcoded secrets
      const secretPatterns = [
        /password\s*=\s*["'][^"']+["']/i,
        /secret\s*=\s*["'][^"']+["']/i,
        /key\s*=\s*["'][^"']+["']/i,
        /token\s*=\s*["'][^"']+["']/i,
      ];

      secretPatterns.forEach(pattern => {
        if (pattern.test(content)) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.CRITICAL,
            location: { file },
            description: `Hardcoded secrets detected in source code`,
            recommendation: `Move secrets to environment variables or secure vault`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      });

      // Check for weak password policies
      if (
        content.includes('password') &&
        !content.includes('BCrypt') &&
        !content.includes('hash')
      ) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.HIGH,
          location: { file },
          description: `Password handling without proper hashing`,
          recommendation: `Use BCrypt or similar strong hashing algorithm for passwords`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.HIGH,
        });
      }
    }
  }

  private async analyzeAuthorization(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const controllerFiles = this.getAllFiles(servicePath, ['.kt']).filter(
      f => f.includes('controller') || f.includes('Controller')
    );

    for (const file of controllerFiles) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for missing authorization on endpoints
      const mappingRegex = /@(Get|Post|Put|Delete|Patch)Mapping/g;
      const mappings = content.match(mappingRegex) || [];

      if (mappings.length > 0) {
        if (
          !content.includes('@PreAuthorize') &&
          !content.includes('@Secured') &&
          !content.includes('@RolesAllowed')
        ) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.HIGH,
            location: { file },
            description: `API endpoints missing authorization checks`,
            recommendation: `Add @PreAuthorize or @Secured annotations to protect endpoints`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }

      // Check for admin endpoints without proper protection
      if (content.includes('/admin') || content.includes('admin')) {
        if (!content.includes('ADMIN') && !content.includes('hasRole')) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.CRITICAL,
            location: { file },
            description: `Admin endpoints without role-based protection`,
            recommendation: `Implement proper role-based access control for admin functions`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }
    }
  }

  private async analyzeInputValidation(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllFiles(servicePath, ['.kt']);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for SQL injection vulnerabilities
      if (
        content.includes('createQuery') ||
        content.includes('createNativeQuery')
      ) {
        if (content.includes('$') || content.includes('+')) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.CRITICAL,
            location: { file },
            description: `Potential SQL injection vulnerability in query construction`,
            recommendation: `Use parameterized queries or prepared statements`,
            estimatedEffort: EffortLevel.MEDIUM,
            businessImpact: ImpactLevel.HIGH,
          });
        }
      }

      // Check for XSS vulnerabilities
      if (content.includes('ResponseEntity') && !content.includes('@Valid')) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Missing input validation on API endpoints`,
          recommendation: `Add @Valid annotation and proper input sanitization`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for path traversal vulnerabilities
      if (content.includes('File(') && content.includes('request')) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.HIGH,
          location: { file },
          description: `Potential path traversal vulnerability in file operations`,
          recommendation: `Validate and sanitize file paths from user input`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.HIGH,
        });
      }
    }
  }

  private async analyzeSensitiveDataExposure(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const files = this.getAllFiles(servicePath, [
      '.kt',
      '.yml',
      '.yaml',
      '.properties',
    ]);

    for (const file of files) {
      const content = fs.readFileSync(file, 'utf8');

      // Check for sensitive data in logs
      const logPatterns = [
        /log.*password/i,
        /log.*secret/i,
        /log.*token/i,
        /println.*password/i,
        /println.*secret/i,
      ];

      logPatterns.forEach(pattern => {
        if (pattern.test(content)) {
          this.addFinding({
            category: FindingCategory.SECURITY,
            severity: Severity.HIGH,
            location: { file },
            description: `Sensitive data potentially logged`,
            recommendation: `Remove sensitive data from log statements`,
            estimatedEffort: EffortLevel.LOW,
            businessImpact: ImpactLevel.MEDIUM,
          });
        }
      });

      // Check for sensitive data in error messages
      if (
        content.includes('Exception') &&
        (content.includes('password') || content.includes('secret'))
      ) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Sensitive data in exception messages`,
          recommendation: `Sanitize error messages to avoid exposing sensitive information`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }

      // Check for debug mode in production configs
      if (file.includes('application') && content.includes('debug: true')) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.MEDIUM,
          location: { file },
          description: `Debug mode enabled in configuration`,
          recommendation: `Disable debug mode in production environments`,
          estimatedEffort: EffortLevel.LOW,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }
    }
  }

  private async analyzeDependencyVulnerabilities(
    servicePath: string,
    serviceName: string
  ): Promise<void> {
    const buildFile = path.join(servicePath, 'build.gradle.kts');
    if (!fs.existsSync(buildFile)) return;

    const content = fs.readFileSync(buildFile, 'utf8');

    // Check for outdated Spring Boot versions
    const springBootMatch = content.match(
      /spring-boot['"]\s*version\s*["']([^"']+)["']/
    );
    if (springBootMatch) {
      const version = springBootMatch[1];
      if (version.startsWith('2.')) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.MEDIUM,
          location: { file: buildFile },
          description: `Outdated Spring Boot version: ${version}`,
          recommendation: `Update to Spring Boot 3.x for latest security patches`,
          estimatedEffort: EffortLevel.HIGH,
          businessImpact: ImpactLevel.MEDIUM,
        });
      }
    }

    // Check for known vulnerable dependencies
    const vulnerableDeps = [
      'log4j:log4j:1.',
      'commons-collections:commons-collections:3.',
      'jackson-databind:2.9.',
    ];

    vulnerableDeps.forEach(vulnDep => {
      if (content.includes(vulnDep)) {
        this.addFinding({
          category: FindingCategory.SECURITY,
          severity: Severity.HIGH,
          location: { file: buildFile },
          description: `Known vulnerable dependency: ${vulnDep}`,
          recommendation: `Update to latest secure version of the dependency`,
          estimatedEffort: EffortLevel.MEDIUM,
          businessImpact: ImpactLevel.HIGH,
        });
      }
    });
  }

  private getAllFiles(dir: string, extensions: string[]): string[] {
    const files: string[] = [];

    const traverse = (currentDir: string) => {
      if (!fs.existsSync(currentDir)) return;

      const items = fs.readdirSync(currentDir, { withFileTypes: true });

      for (const item of items) {
        const fullPath = path.join(currentDir, item.name);

        if (
          item.isDirectory() &&
          !item.name.includes('node_modules') &&
          !item.name.includes('build')
        ) {
          traverse(fullPath);
        } else if (extensions.some(ext => item.name.endsWith(ext))) {
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
      id: `sec-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      agentId: 'security-analyzer',
      timestamp: new Date(),
    });
  }
}
