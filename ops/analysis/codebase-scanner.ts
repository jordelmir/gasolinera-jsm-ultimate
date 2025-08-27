#!/usr/bin/env node

import * as fs from 'fs';
import * as path from 'path';
import { glob } from 'glob';

export interface CodebaseStructure {
  services: ServiceModule[];
  applications: ApplicationModule[];
  packages: PackageModule[];
  infrastructure: InfrastructureModule[];
  tests: TestModule[];
  totalFiles: number;
  totalLinesOfCode: number;
  lastScanned: Date;
}

export interface ServiceModule {
  name: string;
  path: string;
  language: 'kotlin' | 'typescript';
  framework: string;
  dependencies: Dependency[];
  endpoints: ApiEndpoint[];
  database: DatabaseConfig | null;
  dockerFile: string | null;
  buildFile: string;
  sourceFiles: string[];
  testFiles: string[];
  linesOfCode: number;
}

export interface ApplicationModule {
  name: string;
  type: 'web' | 'mobile';
  framework: string;
  path: string;
  components: Component[];
  routes: Route[];
  stateManagement: StateConfig | null;
  packageJson: string;
  sourceFiles: string[];
  testFiles: string[];
  linesOfCode: number;
}

export interface PackageModule {
  name: string;
  path: string;
  type: 'shared' | 'sdk' | 'proto';
  language: string;
  exports: string[];
  dependencies: Dependency[];
  sourceFiles: string[];
  linesOfCode: number;
}

export interface InfrastructureModule {
  name: string;
  path: string;
  type: 'docker' | 'kubernetes' | 'terraform' | 'helm' | 'nginx';
  configFiles: string[];
  environments: string[];
}

export interface TestModule {
  name: string;
  path: string;
  type: 'unit' | 'integration' | 'e2e';
  framework: string;
  testFiles: string[];
  coverage: number | null;
}

export interface Dependency {
  name: string;
  version: string;
  type: 'production' | 'development' | 'peer';
  scope?: string;
}

export interface ApiEndpoint {
  path: string;
  method: string;
  controller: string;
  authenticated: boolean;
}

export interface DatabaseConfig {
  type: string;
  migrations: string[];
  entities: string[];
}

export interface Component {
  name: string;
  path: string;
  type: 'page' | 'component' | 'layout';
  dependencies: string[];
}

export interface Route {
  path: string;
  component: string;
  protected: boolean;
}

export interface StateConfig {
  type: 'zustand' | 'redux' | 'context';
  stores: string[];
}

export class CodebaseScanner {
  private rootPath: string;

  constructor(rootPath: string = process.cwd()) {
    this.rootPath = rootPath;
  }

  async scanCodebase(): Promise<CodebaseStructure> {
    console.log('🔍 Starting codebase scan...');

    const [services, applications, packages, infrastructure, tests] =
      await Promise.all([
        this.scanServices(),
        this.scanApplications(),
        this.scanPackages(),
        this.scanInfrastructure(),
        this.scanTests(),
      ]);

    const totalFiles = this.countTotalFiles([
      ...services,
      ...applications,
      ...packages,
    ]);
    const totalLinesOfCode = this.countTotalLinesOfCode([
      ...services,
      ...applications,
      ...packages,
    ]);

    const structure: CodebaseStructure = {
      services,
      applications,
      packages,
      infrastructure,
      tests,
      totalFiles,
      totalLinesOfCode,
      lastScanned: new Date(),
    };

    console.log(
      `✅ Scan complete: ${totalFiles} files, ${totalLinesOfCode} lines of code`
    );
    return structure;
  }

  private async scanServices(): Promise<ServiceModule[]> {
    const servicesPath = path.join(this.rootPath, 'services');
    if (!fs.existsSync(servicesPath)) return [];

    const serviceDirs = fs
      .readdirSync(servicesPath, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);

    const services: ServiceModule[] = [];

    for (const serviceDir of serviceDirs) {
      const servicePath = path.join(servicesPath, serviceDir);
      const service = await this.analyzeService(serviceDir, servicePath);
      if (service) services.push(service);
    }

    return services;
  }

  private async analyzeService(
    name: string,
    servicePath: string
  ): Promise<ServiceModule | null> {
    try {
      const buildFile = this.findBuildFile(servicePath);
      if (!buildFile) return null;

      const language = buildFile.endsWith('.gradle.kts')
        ? 'kotlin'
        : 'typescript';
      const framework = await this.detectServiceFramework(
        servicePath,
        language
      );

      const sourceFiles = await this.findSourceFiles(servicePath, language);
      const testFiles = await this.findTestFiles(servicePath, language);
      const dependencies = await this.extractDependencies(
        servicePath,
        buildFile
      );
      const endpoints = await this.extractApiEndpoints(servicePath, language);
      const database = await this.extractDatabaseConfig(servicePath);
      const dockerFile = this.findDockerFile(servicePath);
      const linesOfCode = await this.countLinesInFiles(sourceFiles);

      return {
        name,
        path: servicePath,
        language,
        framework,
        dependencies,
        endpoints,
        database,
        dockerFile,
        buildFile,
        sourceFiles,
        testFiles,
        linesOfCode,
      };
    } catch (error) {
      console.warn(`⚠️  Failed to analyze service ${name}:`, error);
      return null;
    }
  }

  private async scanApplications(): Promise<ApplicationModule[]> {
    const appsPath = path.join(this.rootPath, 'apps');
    if (!fs.existsSync(appsPath)) return [];

    const appDirs = fs
      .readdirSync(appsPath, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);

    const applications: ApplicationModule[] = [];

    for (const appDir of appDirs) {
      const appPath = path.join(appsPath, appDir);
      const app = await this.analyzeApplication(appDir, appPath);
      if (app) applications.push(app);
    }

    return applications;
  }

  private async analyzeApplication(
    name: string,
    appPath: string
  ): Promise<ApplicationModule | null> {
    try {
      const packageJsonPath = path.join(appPath, 'package.json');
      if (!fs.existsSync(packageJsonPath)) return null;

      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
      const type = name.includes('mobile') ? 'mobile' : 'web';
      const framework = this.detectAppFramework(packageJson);

      const sourceFiles = await this.findSourceFiles(appPath, 'typescript');
      const testFiles = await this.findTestFiles(appPath, 'typescript');
      const components = await this.extractComponents(appPath);
      const routes = await this.extractRoutes(appPath, framework);
      const stateManagement = await this.detectStateManagement(appPath);
      const linesOfCode = await this.countLinesInFiles(sourceFiles);

      return {
        name,
        type,
        framework,
        path: appPath,
        components,
        routes,
        stateManagement,
        packageJson: packageJsonPath,
        sourceFiles,
        testFiles,
        linesOfCode,
      };
    } catch (error) {
      console.warn(`⚠️  Failed to analyze application ${name}:`, error);
      return null;
    }
  }

  private async scanPackages(): Promise<PackageModule[]> {
    const packagesPath = path.join(this.rootPath, 'packages');
    if (!fs.existsSync(packagesPath)) return [];

    const packageDirs = fs
      .readdirSync(packagesPath, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);

    const packages: PackageModule[] = [];

    for (const packageDir of packageDirs) {
      const packagePath = path.join(packagesPath, packageDir);
      const pkg = await this.analyzePackage(packageDir, packagePath);
      if (pkg) packages.push(pkg);
    }

    return packages;
  }

  private async analyzePackage(
    name: string,
    packagePath: string
  ): Promise<PackageModule | null> {
    try {
      const packageJsonPath = path.join(packagePath, 'package.json');
      const buildGradlePath = path.join(packagePath, 'build.gradle.kts');

      let language = 'typescript';
      let dependencies: Dependency[] = [];

      if (fs.existsSync(packageJsonPath)) {
        const packageJson = JSON.parse(
          fs.readFileSync(packageJsonPath, 'utf8')
        );
        dependencies = this.extractNpmDependencies(packageJson);
      } else if (fs.existsSync(buildGradlePath)) {
        language = 'kotlin';
        dependencies = await this.extractGradleDependencies(buildGradlePath);
      }

      const type = this.detectPackageType(name);
      const sourceFiles = await this.findSourceFiles(packagePath, language);
      const exports = await this.extractPackageExports(packagePath, language);
      const linesOfCode = await this.countLinesInFiles(sourceFiles);

      return {
        name,
        path: packagePath,
        type,
        language,
        exports,
        dependencies,
        sourceFiles,
        linesOfCode,
      };
    } catch (error) {
      console.warn(`⚠️  Failed to analyze package ${name}:`, error);
      return null;
    }
  }

  private async scanInfrastructure(): Promise<InfrastructureModule[]> {
    const infraPath = path.join(this.rootPath, 'infra');
    if (!fs.existsSync(infraPath)) return [];

    const infraDirs = fs
      .readdirSync(infraPath, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);

    const infrastructure: InfrastructureModule[] = [];

    for (const infraDir of infraDirs) {
      const infraModulePath = path.join(infraPath, infraDir);
      const module = await this.analyzeInfrastructure(
        infraDir,
        infraModulePath
      );
      if (module) infrastructure.push(module);
    }

    return infrastructure;
  }

  private async analyzeInfrastructure(
    name: string,
    infraPath: string
  ): Promise<InfrastructureModule | null> {
    try {
      const type = this.detectInfrastructureType(name);
      const configFiles = await this.findConfigFiles(infraPath, type);
      const environments = await this.detectEnvironments(infraPath);

      return {
        name,
        path: infraPath,
        type,
        configFiles,
        environments,
      };
    } catch (error) {
      console.warn(`⚠️  Failed to analyze infrastructure ${name}:`, error);
      return null;
    }
  }

  private async scanTests(): Promise<TestModule[]> {
    const testModules: TestModule[] = [];

    // Scan integration tests
    const integrationTestsPath = path.join(this.rootPath, 'integration-tests');
    if (fs.existsSync(integrationTestsPath)) {
      const testFiles = await glob('**/*.{test,spec}.{ts,js,kt}', {
        cwd: integrationTestsPath,
      });
      testModules.push({
        name: 'integration-tests',
        path: integrationTestsPath,
        type: 'integration',
        framework: 'gradle',
        testFiles: testFiles.map(f => path.join(integrationTestsPath, f)),
        coverage: null,
      });
    }

    // Scan e2e tests in apps
    const appsPath = path.join(this.rootPath, 'apps');
    if (fs.existsSync(appsPath)) {
      const e2eDirs = fs
        .readdirSync(appsPath, { withFileTypes: true })
        .filter(dirent => dirent.isDirectory() && dirent.name.endsWith('-e2e'))
        .map(dirent => dirent.name);

      for (const e2eDir of e2eDirs) {
        const e2ePath = path.join(appsPath, e2eDir);
        const testFiles = await glob('**/*.{test,spec}.{ts,js}', {
          cwd: e2ePath,
        });
        testModules.push({
          name: e2eDir,
          path: e2ePath,
          type: 'e2e',
          framework: 'cypress',
          testFiles: testFiles.map(f => path.join(e2ePath, f)),
          coverage: null,
        });
      }
    }

    return testModules;
  }

  // Helper methods
  private findBuildFile(servicePath: string): string | null {
    const gradleFile = path.join(servicePath, 'build.gradle.kts');
    const packageFile = path.join(servicePath, 'package.json');

    if (fs.existsSync(gradleFile)) return gradleFile;
    if (fs.existsSync(packageFile)) return packageFile;
    return null;
  }

  private async detectServiceFramework(
    servicePath: string,
    language: string
  ): Promise<string> {
    if (language === 'kotlin') {
      const buildFile = path.join(servicePath, 'build.gradle.kts');
      if (fs.existsSync(buildFile)) {
        const content = fs.readFileSync(buildFile, 'utf8');
        if (content.includes('spring-boot')) return 'Spring Boot';
        if (content.includes('ktor')) return 'Ktor';
      }
      return 'Unknown Kotlin Framework';
    } else {
      const packageFile = path.join(servicePath, 'package.json');
      if (fs.existsSync(packageFile)) {
        const packageJson = JSON.parse(fs.readFileSync(packageFile, 'utf8'));
        if (packageJson.dependencies?.express) return 'Express';
        if (packageJson.dependencies?.fastify) return 'Fastify';
        if (packageJson.dependencies?.koa) return 'Koa';
      }
      return 'Unknown Node.js Framework';
    }
  }

  private detectAppFramework(packageJson: any): string {
    if (packageJson.dependencies?.next) return 'Next.js';
    if (packageJson.dependencies?.react) return 'React';
    if (packageJson.dependencies?.['react-native']) return 'React Native';
    if (packageJson.dependencies?.expo) return 'Expo';
    return 'Unknown Framework';
  }

  private detectPackageType(name: string): 'shared' | 'sdk' | 'proto' {
    if (name.includes('sdk')) return 'sdk';
    if (name.includes('proto')) return 'proto';
    return 'shared';
  }

  private detectInfrastructureType(
    name: string
  ): 'docker' | 'kubernetes' | 'terraform' | 'helm' | 'nginx' {
    if (name.includes('docker')) return 'docker';
    if (name.includes('helm')) return 'helm';
    if (name.includes('terraform')) return 'terraform';
    if (name.includes('nginx')) return 'nginx';
    return 'kubernetes';
  }

  private async findSourceFiles(
    basePath: string,
    language: string
  ): Promise<string[]> {
    const extensions =
      language === 'kotlin' ? '**/*.{kt,kts}' : '**/*.{ts,tsx,js,jsx}';
    const files = await glob(extensions, {
      cwd: basePath,
      ignore: [
        '**/node_modules/**',
        '**/build/**',
        '**/dist/**',
        '**/*.test.*',
        '**/*.spec.*',
      ],
    });
    return files.map(f => path.join(basePath, f));
  }

  private async findTestFiles(
    basePath: string,
    language: string
  ): Promise<string[]> {
    const extensions =
      language === 'kotlin'
        ? '**/*{Test,Tests,Spec}.{kt,kts}'
        : '**/*.{test,spec}.{ts,tsx,js,jsx}';
    const files = await glob(extensions, { cwd: basePath });
    return files.map(f => path.join(basePath, f));
  }

  private async findConfigFiles(
    basePath: string,
    type: string
  ): Promise<string[]> {
    let pattern = '**/*.{yml,yaml,json,conf}';

    switch (type) {
      case 'docker':
        pattern = '**/Dockerfile*';
        break;
      case 'kubernetes':
        pattern = '**/*.{yml,yaml}';
        break;
      case 'terraform':
        pattern = '**/*.{tf,tfvars}';
        break;
      case 'helm':
        pattern = '**/*.{yml,yaml,tpl}';
        break;
      case 'nginx':
        pattern = '**/*.{conf,nginx}';
        break;
    }

    const files = await glob(pattern, { cwd: basePath });
    return files.map(f => path.join(basePath, f));
  }

  private findDockerFile(servicePath: string): string | null {
    const dockerFile = path.join(servicePath, 'Dockerfile');
    return fs.existsSync(dockerFile) ? dockerFile : null;
  }

  private async extractDependencies(
    servicePath: string,
    buildFile: string
  ): Promise<Dependency[]> {
    if (buildFile.endsWith('package.json')) {
      const packageJson = JSON.parse(fs.readFileSync(buildFile, 'utf8'));
      return this.extractNpmDependencies(packageJson);
    } else if (buildFile.endsWith('.gradle.kts')) {
      return this.extractGradleDependencies(buildFile);
    }
    return [];
  }

  private extractNpmDependencies(packageJson: any): Dependency[] {
    const dependencies: Dependency[] = [];

    Object.entries(packageJson.dependencies || {}).forEach(
      ([name, version]) => {
        dependencies.push({
          name,
          version: version as string,
          type: 'production',
        });
      }
    );

    Object.entries(packageJson.devDependencies || {}).forEach(
      ([name, version]) => {
        dependencies.push({
          name,
          version: version as string,
          type: 'development',
        });
      }
    );

    return dependencies;
  }

  private async extractGradleDependencies(
    buildFile: string
  ): Promise<Dependency[]> {
    const content = fs.readFileSync(buildFile, 'utf8');
    const dependencies: Dependency[] = [];

    // Simple regex to extract dependencies - could be enhanced
    const depRegex =
      /(?:implementation|api|testImplementation)\s*\(\s*["']([^"']+)["']\s*\)/g;
    let match;

    while ((match = depRegex.exec(content)) !== null) {
      const [, depString] = match;
      const parts = depString.split(':');
      if (parts.length >= 2) {
        dependencies.push({
          name: `${parts[0]}:${parts[1]}`,
          version: parts[2] || 'latest',
          type: 'production',
        });
      }
    }

    return dependencies;
  }

  private async extractApiEndpoints(
    servicePath: string,
    language: string
  ): Promise<ApiEndpoint[]> {
    // This would need more sophisticated parsing - placeholder implementation
    return [];
  }

  private async extractDatabaseConfig(
    servicePath: string
  ): Promise<DatabaseConfig | null> {
    const migrationsPath = path.join(
      servicePath,
      'src/main/resources/db/migration'
    );
    if (fs.existsSync(migrationsPath)) {
      const migrations = fs
        .readdirSync(migrationsPath)
        .filter(f => f.endsWith('.sql'));
      return {
        type: 'PostgreSQL',
        migrations: migrations.map(m => path.join(migrationsPath, m)),
        entities: [], // Would need to parse entity classes
      };
    }
    return null;
  }

  private async extractComponents(appPath: string): Promise<Component[]> {
    // Placeholder - would need more sophisticated parsing
    return [];
  }

  private async extractRoutes(
    appPath: string,
    framework: string
  ): Promise<Route[]> {
    // Placeholder - would need framework-specific parsing
    return [];
  }

  private async detectStateManagement(
    appPath: string
  ): Promise<StateConfig | null> {
    const packageJsonPath = path.join(appPath, 'package.json');
    if (fs.existsSync(packageJsonPath)) {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
      if (packageJson.dependencies?.zustand) {
        return { type: 'zustand', stores: [] };
      }
    }
    return null;
  }

  private async extractPackageExports(
    packagePath: string,
    language: string
  ): Promise<string[]> {
    // Placeholder - would need to parse index files and exports
    return [];
  }

  private async detectEnvironments(infraPath: string): Promise<string[]> {
    const envs = new Set<string>();
    const files = await glob('**/*.{yml,yaml,tf,tfvars}', { cwd: infraPath });

    files.forEach(file => {
      if (file.includes('dev')) envs.add('development');
      if (file.includes('prod')) envs.add('production');
      if (file.includes('staging')) envs.add('staging');
      if (file.includes('test')) envs.add('test');
    });

    return Array.from(envs);
  }

  private async countLinesInFiles(files: string[]): Promise<number> {
    let totalLines = 0;

    for (const file of files) {
      try {
        const content = fs.readFileSync(file, 'utf8');
        totalLines += content.split('\n').length;
      } catch (error) {
        // Skip files that can't be read
      }
    }

    return totalLines;
  }

  private countTotalFiles(
    modules: (ServiceModule | ApplicationModule | PackageModule)[]
  ): number {
    return modules.reduce((total, module) => {
      const testFiles = 'testFiles' in module ? module.testFiles.length : 0;
      return total + module.sourceFiles.length + testFiles;
    }, 0);
  }

  private countTotalLinesOfCode(
    modules: (ServiceModule | ApplicationModule | PackageModule)[]
  ): number {
    return modules.reduce((total, module) => {
      const linesOfCode = 'linesOfCode' in module ? module.linesOfCode : 0;
      return total + linesOfCode;
    }, 0);
  }
}

// CLI usage
if (require.main === module) {
  const scanner = new CodebaseScanner();
  scanner
    .scanCodebase()
    .then(structure => {
      console.log('\n📊 Codebase Structure Summary:');
      console.log(`Services: ${structure.services.length}`);
      console.log(`Applications: ${structure.applications.length}`);
      console.log(`Packages: ${structure.packages.length}`);
      console.log(`Infrastructure modules: ${structure.infrastructure.length}`);
      console.log(`Test modules: ${structure.tests.length}`);
      console.log(`Total files: ${structure.totalFiles}`);
      console.log(`Total lines of code: ${structure.totalLinesOfCode}`);

      // Save results
      const outputPath = path.join(
        process.cwd(),
        'ops/analysis/codebase-structure.json'
      );
      fs.writeFileSync(outputPath, JSON.stringify(structure, null, 2));
      console.log(`\n💾 Results saved to: ${outputPath}`);
    })
    .catch(error => {
      console.error('❌ Scan failed:', error);
      process.exit(1);
    });
}
