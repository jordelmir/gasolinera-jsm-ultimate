# JSM System Analysis Tool

Comprehensive analysis system for the Gasolinera JSM platform that identifies architecture, security, performance, and maintainability issues across the entire codebase.

## 🚀 Quick Start

### Run Complete Analysis

```bash
# From project root
npm run analyze

# Or directly
./ops/analysis/run-analysis.sh
```

### View Results

```bash
# View summary report
npm run analyze:report

# Or open files directly
cat ops/analysis/results/analysis-report.md
```

## 📊 What It Analyzes

### 🏗️ Architecture Analysis

- Spring Boot patterns and annotations
- Dependency injection best practices
- API design and REST conventions
- Layer separation (Controller → Service → Repository)
- Circular dependencies detection

### 🔒 Security Analysis

- JWT implementation and validation
- Hardcoded secrets detection
- Input validation and SQL injection
- Authorization checks on endpoints
- Sensitive data exposure in logs

### ⚡ Performance Analysis

- Database query optimization (N+1 problems)
- Memory usage patterns
- API response optimization
- Caching strategies
- Transaction boundaries

### 🔧 Maintainability Analysis

- Code style consistency
- Type safety (nullable types, Any usage)
- Documentation coverage
- Test coverage and quality
- Method complexity and class size

## 📁 Project Structure

```
ops/analysis/
├── main-analyzer.ts          # Main orchestrator
├── codebase-scanner.ts       # Scans project structure
├── analysis-aggregator.ts    # Consolidates results
├── strategy-coordinator.ts   # Generates improvement plans
├── agents/                   # Specialized analyzers
│   ├── architecture-analyzer.ts
│   ├── security-analyzer.ts
│   ├── performance-analyzer.ts
│   └── maintainability-analyzer.ts
├── results/                  # Generated reports
│   ├── analysis-report.md
│   ├── improvement-plan.json
│   └── consolidated-analysis.json
└── run-analysis.sh          # Execution script
```

## 🎯 Understanding Results

### Analysis Report (`analysis-report.md`)

- Executive summary with key metrics
- Issues categorized by type and severity
- Critical issues requiring immediate attention
- Quick wins for easy improvements
- Detailed improvement plan with phases

### Improvement Plan (`improvement-plan.json`)

- Structured phases for implementation
- Task breakdown with effort estimates
- Dependencies between improvements
- Timeline and resource planning

### Consolidated Analysis (`consolidated-analysis.json`)

- Raw findings from all analyzers
- Detailed location and recommendation data
- Severity and impact assessments
- Agent execution metadata

## 🔧 Configuration

### Analysis Scope

The tool automatically analyzes:

- All services in `services/` directory
- All applications in `apps/` directory
- Shared packages in `packages/` directory
- Infrastructure code in `infra/` directory

### Customization

Edit the analyzer classes in `agents/` to:

- Add new analysis rules
- Modify severity assessments
- Customize recommendations
- Add new finding categories

## 📈 Improvement Workflow

### Phase 1: Critical Issues (Week 1-2)

- Address security vulnerabilities
- Fix performance bottlenecks
- Implement quick wins

### Phase 2: Security Enhancements (Week 3-5)

- Comprehensive security hardening
- Input validation improvements
- Authentication/authorization fixes

### Phase 3: Performance Optimization (Week 6-9)

- Database query optimization
- Caching implementation
- API performance improvements

### Phase 4: Architecture Refactoring (Week 10-14)

- Service boundary improvements
- Design pattern implementation
- Dependency management

### Phase 5: Code Quality (Week 15-17)

- Documentation improvements
- Test coverage increase
- Code style standardization

## 🛠️ Development

### Adding New Analysis Rules

1. **Choose the appropriate analyzer** (`architecture`, `security`, `performance`, `maintainability`)

2. **Add detection logic**:

```typescript
// In the relevant analyzer
private async analyzeNewPattern(servicePath: string): Promise<void> {
  const files = this.getAllKotlinFiles(servicePath);

  for (const file of files) {
    const content = fs.readFileSync(file, 'utf8');

    if (content.includes('problematic-pattern')) {
      this.addFinding({
        category: FindingCategory.ARCHITECTURE,
        severity: Severity.MEDIUM,
        location: { file },
        description: 'Description of the issue',
        recommendation: 'How to fix it',
        estimatedEffort: EffortLevel.LOW,
        businessImpact: ImpactLevel.MEDIUM
      });
    }
  }
}
```

3. **Call the new analysis** in the main `analyzeService` method

### Testing Changes

```bash
# Run analysis on specific service
cd ops/analysis
npx ts-node -e "
const analyzer = require('./main-analyzer').MainAnalyzer;
new analyzer().analyzeService('./services/auth-service', 'auth-service');
"

# Run full analysis
npm run analyze
```

## 📋 Troubleshooting

### Common Issues

**No services found**

- Ensure you're running from the project root
- Check that `services/` directory exists
- Verify service directories contain `build.gradle.kts` or `package.json`

**TypeScript compilation errors**

- Run `npm install` in `ops/analysis/`
- Check `tsconfig.json` configuration
- Verify all imports are correct

**Analysis takes too long**

- Use `npm run analyze:quick` for faster analysis
- Exclude large directories in scanner configuration
- Run analysis on specific modules only

### Getting Help

1. Check the console output for specific error messages
2. Review the generated logs in `ops/analysis/results/`
3. Verify file permissions on analysis scripts
4. Ensure all dependencies are installed

## 🎉 Success Metrics

Track improvement progress with:

- **Reduced critical issues** from analysis reports
- **Improved test coverage** percentages
- **Faster build times** after optimizations
- **Better code review feedback** from team
- **Reduced bug reports** in production

---

_Generated by JSM System Analysis Tool v1.0.0_
