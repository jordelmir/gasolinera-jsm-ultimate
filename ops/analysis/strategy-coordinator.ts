import * as fs from 'fs';
import * as path from 'path';
import {
  Finding,
  FindingCategory,
  Severity,
  EffortLevel,
  ImpactLevel,
  AnalysisResult,
  ConsolidatedResults,
} from './analysis-aggregator';

export interface ImprovementPlan {
  phases: ImprovementPhase[];
  quickWins: QuickWin[];
  majorInitiatives: MajorInitiative[];
  estimatedTimeline: Timeline;
  priorityMatrix: PriorityMatrix;
}

export interface ImprovementPhase {
  name: string;
  description: string;
  tasks: ImprovementTask[];
  dependencies: string[];
  estimatedDuration: number; // in days
  priority: number;
}

export interface ImprovementTask {
  id: string;
  title: string;
  description: string;
  category: FindingCategory;
  files: string[];
  changes: CodeChange[];
  tests: TestRequirement[];
  estimatedHours: number;
  findingIds: string[];
}

export interface QuickWin {
  id: string;
  title: string;
  description: string;
  impact: ImpactLevel;
  effort: EffortLevel;
  estimatedHours: number;
  files: string[];
  findingIds: string[];
}

export interface MajorInitiative {
  id: string;
  title: string;
  description: string;
  businessValue: string;
  estimatedWeeks: number;
  phases: string[];
  findingIds: string[];
}

export interface Timeline {
  totalWeeks: number;
  phases: { name: string; startWeek: number; duration: number }[];
}

export interface PriorityMatrix {
  critical: Finding[];
  highImpactLowEffort: Finding[];
  highImpactHighEffort: Finding[];
  lowImpactLowEffort: Finding[];
  lowImpactHighEffort: Finding[];
}

export interface CodeChange {
  type: 'add' | 'modify' | 'delete' | 'refactor';
  description: string;
  location: string;
}

export interface TestRequirement {
  type: 'unit' | 'integration' | 'e2e';
  description: string;
  coverage: string;
}

export class StrategyCoordinator {
  private findings: Finding[] = [];
  private results: AnalysisResult[] = [];

  consolidateFindings(results: AnalysisResult[]): ConsolidatedResults {
    this.results = results;
    this.findings = results.flatMap(r => r.findings);

    console.log(
      `📊 Consolidating ${this.findings.length} findings from ${results.length} analysis results`
    );

    // Remove duplicates
    const uniqueFindings = this.deduplicateFindings(this.findings);

    // Resolve conflicts
    const resolvedFindings = this.resolveConflicts(uniqueFindings);

    const consolidated: ConsolidatedResults = {
      totalFindings: resolvedFindings.length,
      findingsByCategory: this.groupByCategory(resolvedFindings),
      findingsBySeverity: this.groupBySeverity(resolvedFindings),
      criticalIssues: resolvedFindings.filter(
        f => f.severity === Severity.CRITICAL
      ),
      quickWins: this.identifyQuickWins(resolvedFindings),
      results: this.results,
      lastUpdated: new Date(),
    };

    this.findings = resolvedFindings;
    return consolidated;
  }

  generateImprovementPlan(): ImprovementPlan {
    console.log(
      `🎯 Generating improvement plan from ${this.findings.length} findings`
    );

    const priorityMatrix = this.createPriorityMatrix();
    const quickWins = this.generateQuickWins();
    const majorInitiatives = this.generateMajorInitiatives();
    const phases = this.generatePhases(quickWins, majorInitiatives);
    const timeline = this.calculateTimeline(phases);

    return {
      phases,
      quickWins,
      majorInitiatives,
      estimatedTimeline: timeline,
      priorityMatrix,
    };
  }

  private deduplicateFindings(findings: Finding[]): Finding[] {
    const seen = new Map<string, Finding>();

    for (const finding of findings) {
      const key = `${finding.location.file}-${finding.description}`;

      if (!seen.has(key) || this.isHigherPriority(finding, seen.get(key)!)) {
        seen.set(key, finding);
      }
    }

    return Array.from(seen.values());
  }

  private resolveConflicts(findings: Finding[]): Finding[] {
    // Group findings by file and look for conflicts
    const byFile = new Map<string, Finding[]>();

    findings.forEach(finding => {
      const file = finding.location.file;
      if (!byFile.has(file)) {
        byFile.set(file, []);
      }
      byFile.get(file)!.push(finding);
    });

    const resolved: Finding[] = [];

    byFile.forEach(fileFindings => {
      // Check for conflicting recommendations
      const conflicts = this.detectConflicts(fileFindings);

      if (conflicts.length > 0) {
        // Resolve by keeping higher severity findings
        const nonConflicting = fileFindings.filter(f => !conflicts.includes(f));
        const highestSeverity = this.getHighestSeverityFinding(conflicts);
        resolved.push(...nonConflicting, highestSeverity);
      } else {
        resolved.push(...fileFindings);
      }
    });

    return resolved;
  }

  private detectConflicts(findings: Finding[]): Finding[] {
    const conflicts: Finding[] = [];

    // Simple conflict detection - could be enhanced
    for (let i = 0; i < findings.length; i++) {
      for (let j = i + 1; j < findings.length; j++) {
        const f1 = findings[i];
        const f2 = findings[j];

        // Check if recommendations conflict
        if (
          this.recommendationsConflict(f1.recommendation, f2.recommendation)
        ) {
          conflicts.push(f1, f2);
        }
      }
    }

    return [...new Set(conflicts)];
  }

  private recommendationsConflict(rec1: string, rec2: string): boolean {
    // Simple conflict detection - could be enhanced with NLP
    const conflictPairs = [
      ['synchronous', 'asynchronous'],
      ['eager', 'lazy'],
      ['cache', 'no cache'],
      ['public', 'private'],
    ];

    return conflictPairs.some(
      ([term1, term2]) =>
        (rec1.toLowerCase().includes(term1) &&
          rec2.toLowerCase().includes(term2)) ||
        (rec1.toLowerCase().includes(term2) &&
          rec2.toLowerCase().includes(term1))
    );
  }

  private createPriorityMatrix(): PriorityMatrix {
    return {
      critical: this.findings.filter(f => f.severity === Severity.CRITICAL),
      highImpactLowEffort: this.findings.filter(
        f =>
          f.businessImpact === ImpactLevel.HIGH &&
          f.estimatedEffort === EffortLevel.LOW
      ),
      highImpactHighEffort: this.findings.filter(
        f =>
          f.businessImpact === ImpactLevel.HIGH &&
          f.estimatedEffort === EffortLevel.HIGH
      ),
      lowImpactLowEffort: this.findings.filter(
        f =>
          f.businessImpact === ImpactLevel.LOW &&
          f.estimatedEffort === EffortLevel.LOW
      ),
      lowImpactHighEffort: this.findings.filter(
        f =>
          f.businessImpact === ImpactLevel.LOW &&
          f.estimatedEffort === EffortLevel.HIGH
      ),
    };
  }

  private generateQuickWins(): QuickWin[] {
    const quickWinFindings = this.findings.filter(
      f =>
        f.estimatedEffort === EffortLevel.LOW &&
        (f.businessImpact === ImpactLevel.MEDIUM ||
          f.businessImpact === ImpactLevel.HIGH)
    );

    return quickWinFindings.map(finding => ({
      id: `qw-${finding.id}`,
      title: this.generateTaskTitle(finding),
      description: finding.description,
      impact: finding.businessImpact,
      effort: finding.estimatedEffort,
      estimatedHours: this.estimateHours(finding.estimatedEffort),
      files: [finding.location.file],
      findingIds: [finding.id],
    }));
  }

  private generateMajorInitiatives(): MajorInitiative[] {
    const majorFindings = this.findings.filter(
      f =>
        f.estimatedEffort === EffortLevel.HIGH ||
        f.severity === Severity.CRITICAL
    );

    // Group by category for major initiatives
    const byCategory = new Map<FindingCategory, Finding[]>();
    majorFindings.forEach(finding => {
      if (!byCategory.has(finding.category)) {
        byCategory.set(finding.category, []);
      }
      byCategory.get(finding.category)!.push(finding);
    });

    const initiatives: MajorInitiative[] = [];

    byCategory.forEach((findings, category) => {
      if (findings.length >= 3) {
        // Only create initiative if enough findings
        initiatives.push({
          id: `mi-${category}`,
          title: this.generateInitiativeTitle(category),
          description: this.generateInitiativeDescription(category, findings),
          businessValue: this.calculateBusinessValue(findings),
          estimatedWeeks: Math.ceil(findings.length * 2), // 2 weeks per major finding
          phases: [`phase-${category}-1`, `phase-${category}-2`],
          findingIds: findings.map(f => f.id),
        });
      }
    });

    return initiatives;
  }

  private generatePhases(
    quickWins: QuickWin[],
    majorInitiatives: MajorInitiative[]
  ): ImprovementPhase[] {
    const phases: ImprovementPhase[] = [];

    // Phase 1: Quick Wins and Critical Issues
    const criticalFindings = this.findings.filter(
      f => f.severity === Severity.CRITICAL
    );
    phases.push({
      name: 'Critical Issues and Quick Wins',
      description:
        'Address critical security and performance issues, implement quick wins',
      tasks: [
        ...this.generateTasksFromFindings(criticalFindings),
        ...this.generateTasksFromQuickWins(quickWins.slice(0, 10)),
      ],
      dependencies: [],
      estimatedDuration: 14, // 2 weeks
      priority: 1,
    });

    // Phase 2: Security Improvements
    const securityFindings = this.findings.filter(
      f =>
        f.category === FindingCategory.SECURITY &&
        f.severity !== Severity.CRITICAL
    );
    if (securityFindings.length > 0) {
      phases.push({
        name: 'Security Enhancements',
        description: 'Implement comprehensive security improvements',
        tasks: this.generateTasksFromFindings(securityFindings),
        dependencies: ['Critical Issues and Quick Wins'],
        estimatedDuration: 21, // 3 weeks
        priority: 2,
      });
    }

    // Phase 3: Performance Optimization
    const performanceFindings = this.findings.filter(
      f => f.category === FindingCategory.PERFORMANCE
    );
    if (performanceFindings.length > 0) {
      phases.push({
        name: 'Performance Optimization',
        description: 'Optimize database queries, caching, and API performance',
        tasks: this.generateTasksFromFindings(performanceFindings),
        dependencies: ['Critical Issues and Quick Wins'],
        estimatedDuration: 28, // 4 weeks
        priority: 3,
      });
    }

    // Phase 4: Architecture Improvements
    const architectureFindings = this.findings.filter(
      f => f.category === FindingCategory.ARCHITECTURE
    );
    if (architectureFindings.length > 0) {
      phases.push({
        name: 'Architecture Refactoring',
        description:
          'Improve service boundaries, dependency injection, and design patterns',
        tasks: this.generateTasksFromFindings(architectureFindings),
        dependencies: ['Security Enhancements', 'Performance Optimization'],
        estimatedDuration: 35, // 5 weeks
        priority: 4,
      });
    }

    // Phase 5: Maintainability Improvements
    const maintainabilityFindings = this.findings.filter(
      f => f.category === FindingCategory.MAINTAINABILITY
    );
    if (maintainabilityFindings.length > 0) {
      phases.push({
        name: 'Code Quality and Documentation',
        description: 'Improve code quality, documentation, and test coverage',
        tasks: this.generateTasksFromFindings(maintainabilityFindings),
        dependencies: ['Architecture Refactoring'],
        estimatedDuration: 21, // 3 weeks
        priority: 5,
      });
    }

    return phases;
  }

  private generateTasksFromFindings(findings: Finding[]): ImprovementTask[] {
    return findings.map(finding => ({
      id: `task-${finding.id}`,
      title: this.generateTaskTitle(finding),
      description: finding.recommendation,
      category: finding.category,
      files: [finding.location.file],
      changes: this.generateCodeChanges(finding),
      tests: this.generateTestRequirements(finding),
      estimatedHours: this.estimateHours(finding.estimatedEffort),
      findingIds: [finding.id],
    }));
  }

  private generateTasksFromQuickWins(quickWins: QuickWin[]): ImprovementTask[] {
    return quickWins.map(qw => ({
      id: `task-${qw.id}`,
      title: qw.title,
      description: qw.description,
      category: FindingCategory.MAINTAINABILITY, // Default for quick wins
      files: qw.files,
      changes: [
        { type: 'modify', description: qw.description, location: qw.files[0] },
      ],
      tests: [
        {
          type: 'unit',
          description: 'Verify quick win implementation',
          coverage: 'modified code',
        },
      ],
      estimatedHours: qw.estimatedHours,
      findingIds: qw.findingIds,
    }));
  }

  private calculateTimeline(phases: ImprovementPhase[]): Timeline {
    let currentWeek = 0;
    const phaseTimeline: {
      name: string;
      startWeek: number;
      duration: number;
    }[] = [];

    phases.forEach(phase => {
      const durationWeeks = Math.ceil(phase.estimatedDuration / 7);
      phaseTimeline.push({
        name: phase.name,
        startWeek: currentWeek,
        duration: durationWeeks,
      });
      currentWeek += durationWeeks;
    });

    return {
      totalWeeks: currentWeek,
      phases: phaseTimeline,
    };
  }

  // Helper methods
  private isHigherPriority(f1: Finding, f2: Finding): boolean {
    const severityOrder = { critical: 4, high: 3, medium: 2, low: 1 };
    return severityOrder[f1.severity] > severityOrder[f2.severity];
  }

  private getHighestSeverityFinding(findings: Finding[]): Finding {
    return findings.reduce((highest, current) =>
      this.isHigherPriority(current, highest) ? current : highest
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

  private generateTaskTitle(finding: Finding): string {
    const category =
      finding.category.charAt(0).toUpperCase() + finding.category.slice(1);
    return `${category}: ${finding.description.substring(0, 50)}...`;
  }

  private generateInitiativeTitle(category: FindingCategory): string {
    const titles = {
      [FindingCategory.ARCHITECTURE]: 'Architecture Modernization',
      [FindingCategory.SECURITY]: 'Security Hardening',
      [FindingCategory.PERFORMANCE]: 'Performance Optimization',
      [FindingCategory.MAINTAINABILITY]: 'Code Quality Enhancement',
    };
    return titles[category];
  }

  private generateInitiativeDescription(
    category: FindingCategory,
    findings: Finding[]
  ): string {
    return `Comprehensive ${category} improvements addressing ${findings.length} identified issues`;
  }

  private calculateBusinessValue(findings: Finding[]): string {
    const highImpact = findings.filter(
      f => f.businessImpact === ImpactLevel.HIGH
    ).length;
    const mediumImpact = findings.filter(
      f => f.businessImpact === ImpactLevel.MEDIUM
    ).length;

    if (highImpact > mediumImpact) {
      return 'High business value through improved system reliability and performance';
    } else {
      return 'Medium business value through enhanced code quality and maintainability';
    }
  }

  private generateCodeChanges(finding: Finding): CodeChange[] {
    // Simple mapping - could be enhanced with more sophisticated analysis
    const changeTypes: Record<FindingCategory, CodeChange['type']> = {
      [FindingCategory.ARCHITECTURE]: 'refactor',
      [FindingCategory.SECURITY]: 'modify',
      [FindingCategory.PERFORMANCE]: 'modify',
      [FindingCategory.MAINTAINABILITY]: 'modify',
    };

    return [
      {
        type: changeTypes[finding.category],
        description: finding.recommendation,
        location: finding.location.file,
      },
    ];
  }

  private generateTestRequirements(finding: Finding): TestRequirement[] {
    const requirements: TestRequirement[] = [];

    if (finding.category === FindingCategory.SECURITY) {
      requirements.push({
        type: 'unit',
        description: 'Verify security implementation',
        coverage: 'security-related code',
      });
    }

    if (finding.category === FindingCategory.PERFORMANCE) {
      requirements.push({
        type: 'integration',
        description: 'Verify performance improvements',
        coverage: 'optimized code paths',
      });
    }

    return requirements;
  }

  private estimateHours(effort: EffortLevel): number {
    const hoursByEffort = {
      [EffortLevel.LOW]: 4,
      [EffortLevel.MEDIUM]: 16,
      [EffortLevel.HIGH]: 40,
    };
    return hoursByEffort[effort];
  }
}
