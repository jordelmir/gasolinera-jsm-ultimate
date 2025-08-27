#!/usr/bin/env node

import * as fs from 'fs';
import * as path from 'path';
import { CodebaseScanner } from './codebase-scanner';
import { AnalysisAggregator, AnalysisResult } from './analysis-aggregator';
import { ArchitectureAnalyzer } from './agents/architecture-analyzer';
import { SecurityAnalyzer } from './agents/security-analyzer';
import { PerformanceAnalyzer } from './agents/performance-analyzer';
import { MaintainabilityAnalyzer } from './agents/maintainability-analyzer';
import { StrategyCoordinator } from './strategy-coordinator';

export class MainAnalyzer {
  private scanner: CodebaseScanner;
  private aggregator: AnalysisAggregator;
  private coordinator: StrategyCoordinator;
  private architectureAnalyzer: ArchitectureAnalyzer;
  private securityAnalyzer: SecurityAnalyzer;
  private performanceAnalyzer: PerformanceAnalyzer;
  private maintainabilityAnalyzer: MaintainabilityAnalyzer;

  constructor(rootPath: string = process.cwd()) {
    this.scanner = new CodebaseScanner(rootPath);
    this.aggregator = new AnalysisAggregator();
    this.coordinator = new StrategyCoordinator();
    this.architectureAnalyzer = new ArchitectureAnalyzer();
    this.securityAnalyzer = new SecurityAnalyzer();
    this.performanceAnalyzer = new PerformanceAnalyzer();
    this.maintainabilityAnalyzer = new MaintainabilityAnalyzer();
  }

  async runCompleteAnalysis(): Promise<void> {
    console.log('🚀 Starting complete JSM system analysis...');
    const startTime = Date.now();

    try {
      // Step 1: Scan codebase
      console.log('\n📊 Step 1: Scanning codebase structure...');
      const structure = await this.scanner.scanCodebase();

      // Step 2: Analyze each service
      console.log('\n🔍 Step 2: Analyzing services...');
      const results: AnalysisResult[] = [];

      for (const service of structure.services) {
        console.log(`\n  Analyzing service: ${service.name}`);
        const serviceResults = await this.analyzeService(
          service.path,
          service.name
        );
        results.push(...serviceResults);
      }

      // Step 3: Consolidate findings
      console.log('\n📋 Step 3: Consolidating findings...');
      const consolidated = this.coordinator.consolidateFindings(results);

      // Step 4: Generate improvement plan
      console.log('\n🎯 Step 4: Generating improvement plan...');
      const plan = this.coordinator.generateImprovementPlan();

      // Step 5: Save results
      console.log('\n💾 Step 5: Saving results...');
      this.saveResults(consolidated, plan);

      const duration = (Date.now() - startTime) / 1000;
      console.log(`\n✅ Analysis complete in ${duration}s`);
      console.log(`📊 Found ${consolidated.totalFindings} issues`);
      console.log(`🎯 Generated ${plan.phases.length} improvement phases`);
      console.log(`⚡ Identified ${plan.quickWins.length} quick wins`);
    } catch (error) {
      console.error('❌ Analysis failed:', error);
      throw error;
    }
  }

  private async analyzeService(
    servicePath: string,
    serviceName: string
  ): Promise<AnalysisResult[]> {
    const results: AnalysisResult[] = [];
    const startTime = Date.now();

    // Run all analyzers in parallel
    const [archFindings, secFindings, perfFindings, maintFindings] =
      await Promise.all([
        this.architectureAnalyzer.analyzeService(servicePath, serviceName),
        this.securityAnalyzer.analyzeService(servicePath, serviceName),
        this.performanceAnalyzer.analyzeService(servicePath, serviceName),
        this.maintainabilityAnalyzer.analyzeService(servicePath, serviceName),
      ]);

    const executionTime = Date.now() - startTime;

    // Create results for each analyzer
    results.push({
      agentId: 'architecture-analyzer',
      agentRole: 'Software Architect',
      module: serviceName,
      findings: archFindings,
      summary: `Found ${archFindings.length} architecture issues`,
      executionTime,
      timestamp: new Date(),
    });

    results.push({
      agentId: 'security-analyzer',
      agentRole: 'Security Expert',
      module: serviceName,
      findings: secFindings,
      summary: `Found ${secFindings.length} security issues`,
      executionTime,
      timestamp: new Date(),
    });

    results.push({
      agentId: 'performance-analyzer',
      agentRole: 'Performance Expert',
      module: serviceName,
      findings: perfFindings,
      summary: `Found ${perfFindings.length} performance issues`,
      executionTime,
      timestamp: new Date(),
    });

    results.push({
      agentId: 'maintainability-analyzer',
      agentRole: 'Maintainability Expert',
      module: serviceName,
      findings: maintFindings,
      summary: `Found ${maintFindings.length} maintainability issues`,
      executionTime,
      timestamp: new Date(),
    });

    // Add results to aggregator
    results.forEach(result => this.aggregator.addResult(result));

    return results;
  }

  private saveResults(consolidated: any, plan: any): void {
    const resultsDir = path.resolve(__dirname, 'results');
    if (!fs.existsSync(resultsDir)) {
      fs.mkdirSync(resultsDir, { recursive: true });
    }

    // Save consolidated results
    fs.writeFileSync(
      path.join(resultsDir, 'consolidated-analysis.json'),
      JSON.stringify(consolidated, null, 2)
    );

    // Save improvement plan
    fs.writeFileSync(
      path.join(resultsDir, 'improvement-plan.json'),
      JSON.stringify(plan, null, 2)
    );

    // Generate summary report
    const report = this.generateSummaryReport(consolidated, plan);
    fs.writeFileSync(path.join(resultsDir, 'analysis-report.md'), report);

    console.log(`📁 Results saved to: ${resultsDir}`);
  }

  private generateSummaryReport(consolidated: any, plan: any): string {
    return `# JSM System Analysis Report

Generated: ${new Date().toISOString()}

## Executive Summary

This comprehensive analysis of the JSM (Gasolinera JSM) system identified **${consolidated.totalFindings}** issues across ${Object.keys(consolidated.findingsByCategory).length} categories.

## Key Findings

### By Category
${Object.entries(consolidated.findingsByCategory)
  .map(([category, count]) => `- **${category}**: ${count} issues`)
  .join('\n')}

### By Severity
${Object.entries(consolidated.findingsBySeverity)
  .map(([severity, count]) => `- **${severity}**: ${count} issues`)
  .join('\n')}

## Critical Issues (${consolidated.criticalIssues.length})

${consolidated.criticalIssues
  .slice(0, 10)
  .map(
    (issue: any, i: number) =>
      `${i + 1}. **${issue.description}** (${path.basename(issue.location.file)})`
  )
  .join('\n')}

## Improvement Plan

### Timeline: ${plan.estimatedTimeline.totalWeeks} weeks

${plan.phases
  .map(
    (phase: any, i: number) => `
#### Phase ${i + 1}: ${phase.name}
- **Duration**: ${phase.estimatedDuration} days
- **Tasks**: ${phase.tasks.length}
- **Description**: ${phase.description}
`
  )
  .join('')}

### Quick Wins (${plan.quickWins.length})

${plan.quickWins
  .slice(0, 10)
  .map(
    (qw: any, i: number) => `${i + 1}. **${qw.title}** (${qw.estimatedHours}h)`
  )
  .join('\n')}

### Major Initiatives (${plan.majorInitiatives.length})

${plan.majorInitiatives
  .map(
    (mi: any, i: number) =>
      `${i + 1}. **${mi.title}** (${mi.estimatedWeeks} weeks)\n   ${mi.businessValue}`
  )
  .join('\n')}

## Next Steps

1. **Review this report** with the development team
2. **Prioritize quick wins** for immediate implementation
3. **Plan Phase 1** execution (Critical Issues and Quick Wins)
4. **Set up monitoring** for improvement metrics
5. **Schedule regular reviews** to track progress

---
*Generated by JSM System Analysis Tool*
`;
  }
}

// CLI execution
if (require.main === module) {
  const analyzer = new MainAnalyzer();
  analyzer
    .runCompleteAnalysis()
    .then(() => {
      console.log('\n🎉 Analysis completed successfully!');
      console.log('📖 Check ops/analysis/results/ for detailed reports');
      process.exit(0);
    })
    .catch(error => {
      console.error('\n💥 Analysis failed:', error);
      process.exit(1);
    });
}
