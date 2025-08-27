import * as fs from 'fs';
import * as path from 'path';

export interface Finding {
  id: string;
  category: FindingCategory;
  severity: Severity;
  location: CodeLocation;
  description: string;
  recommendation: string;
  estimatedEffort: EffortLevel;
  businessImpact: ImpactLevel;
  agentId: string;
  timestamp: Date;
}

export enum FindingCategory {
  ARCHITECTURE = 'architecture',
  SECURITY = 'security',
  PERFORMANCE = 'performance',
  MAINTAINABILITY = 'maintainability',
}

export enum Severity {
  CRITICAL = 'critical',
  HIGH = 'high',
  MEDIUM = 'medium',
  LOW = 'low',
}

export enum EffortLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
}

export enum ImpactLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
}

export interface CodeLocation {
  file: string;
  line?: number;
  column?: number;
  function?: string;
  class?: string;
}

export interface AnalysisResult {
  agentId: string;
  agentRole: string;
  module: string;
  findings: Finding[];
  summary: string;
  executionTime: number;
  timestamp: Date;
}

export interface ConsolidatedResults {
  totalFindings: number;
  findingsByCategory: Record<FindingCategory, number>;
  findingsBySeverity: Record<Severity, number>;
  criticalIssues: Finding[];
  quickWins: Finding[];
  results: AnalysisResult[];
  lastUpdated: Date;
}

export class AnalysisAggregator {
  private resultsPath: string;
  private results: AnalysisResult[] = [];

  constructor(resultsPath: string = 'ops/analysis/results') {
    this.resultsPath = resultsPath;
    this.ensureResultsDirectory();
  }

  private ensureResultsDirectory(): void {
    if (!fs.existsSync(this.resultsPath)) {
      fs.mkdirSync(this.resultsPath, { recursive: true });
    }
  }

  addResult(result: AnalysisResult): void {
    this.results.push(result);
    this.saveResult(result);
  }

  private saveResult(result: AnalysisResult): void {
    const filename = `${result.agentId}-${result.module}-${Date.now()}.json`;
    const filepath = path.join(this.resultsPath, filename);
    fs.writeFileSync(filepath, JSON.stringify(result, null, 2));
  }

  loadAllResults(): AnalysisResult[] {
    const files = fs
      .readdirSync(this.resultsPath)
      .filter(f => f.endsWith('.json') && f !== 'consolidated.json');

    this.results = files.map(file => {
      const filepath = path.join(this.resultsPath, file);
      return JSON.parse(fs.readFileSync(filepath, 'utf8'));
    });

    return this.results;
  }

  consolidateResults(): ConsolidatedResults {
    const allFindings = this.results.flatMap(r => r.findings);

    const consolidated: ConsolidatedResults = {
      totalFindings: allFindings.length,
      findingsByCategory: this.groupByCategory(allFindings),
      findingsBySeverity: this.groupBySeverity(allFindings),
      criticalIssues: allFindings.filter(f => f.severity === Severity.CRITICAL),
      quickWins: this.identifyQuickWins(allFindings),
      results: this.results,
      lastUpdated: new Date(),
    };

    this.saveConsolidatedResults(consolidated);
    return consolidated;
  }

  private groupByCategory(
    findings: Finding[]
  ): Record<FindingCategory, number> {
    return findings.reduce(
      (acc, finding) => {
        acc[finding.category] = (acc[finding.category] || 0) + 1;
        return acc;
      },
      {} as Record<FindingCategory, number>
    );
  }

  private groupBySeverity(findings: Finding[]): Record<Severity, number> {
    return findings.reduce(
      (acc, finding) => {
        acc[finding.severity] = (acc[finding.severity] || 0) + 1;
        return acc;
      },
      {} as Record<Severity, number>
    );
  }

  private identifyQuickWins(findings: Finding[]): Finding[] {
    return findings.filter(
      f =>
        f.estimatedEffort === EffortLevel.LOW &&
        (f.businessImpact === ImpactLevel.MEDIUM ||
          f.businessImpact === ImpactLevel.HIGH)
    );
  }

  private saveConsolidatedResults(consolidated: ConsolidatedResults): void {
    const filepath = path.join(this.resultsPath, 'consolidated.json');
    fs.writeFileSync(filepath, JSON.stringify(consolidated, null, 2));
  }

  generateSummaryReport(): string {
    const consolidated = this.consolidateResults();

    return `
# Analysis Summary Report

Generated: ${consolidated.lastUpdated.toISOString()}

## Overview
- **Total Findings**: ${consolidated.totalFindings}
- **Critical Issues**: ${consolidated.criticalIssues.length}
- **Quick Wins**: ${consolidated.quickWins.length}

## Findings by Category
${Object.entries(consolidated.findingsByCategory)
  .map(([category, count]) => `- **${category}**: ${count}`)
  .join('\n')}

## Findings by Severity
${Object.entries(consolidated.findingsBySeverity)
  .map(([severity, count]) => `- **${severity}**: ${count}`)
  .join('\n')}

## Critical Issues (Top 10)
${consolidated.criticalIssues
  .slice(0, 10)
  .map(
    (finding, i) =>
      `${i + 1}. **${finding.description}** (${finding.location.file})`
  )
  .join('\n')}

## Quick Wins (Top 10)
${consolidated.quickWins
  .slice(0, 10)
  .map(
    (finding, i) =>
      `${i + 1}. **${finding.description}** (${finding.location.file})`
  )
  .join('\n')}
`;
  }

  clearResults(): void {
    const files = fs.readdirSync(this.resultsPath);
    files.forEach(file => {
      fs.unlinkSync(path.join(this.resultsPath, file));
    });
    this.results = [];
  }
}
