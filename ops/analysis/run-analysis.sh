#!/bin/bash

# JSM System Analysis Runner
# Comprehensive analysis of the entire JSM platform

set -e

echo "🚀 JSM System Analysis Starting..."
echo "=================================="

# Check if we're in the right directory
if [ ! -f "package.json" ] || [ ! -d "services" ]; then
    echo "❌ Error: Please run this script from the JSM root directory"
    exit 1
fi

# Create analysis directory if it doesn't exist
mkdir -p ops/analysis/results

# Install dependencies if needed
if [ ! -d "ops/analysis/node_modules" ]; then
    echo "📦 Installing analysis dependencies..."
    cd ops/analysis
    npm install
    cd ../..
fi

# Run the analysis
echo "🔍 Running comprehensive analysis..."
cd ops/analysis
npm run analyze

# Check if analysis was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Analysis completed successfully!"
    echo ""
    echo "📊 Results available in:"
    echo "  - ops/analysis/results/analysis-report.md"
    echo "  - ops/analysis/results/improvement-plan.json"
    echo "  - ops/analysis/results/consolidated-analysis.json"
    echo ""
    echo "🎯 Next steps:"
    echo "  1. Review the analysis report"
    echo "  2. Prioritize quick wins"
    echo "  3. Plan improvement phases"
    echo ""
else
    echo "❌ Analysis failed. Check the logs above for details."
    exit 1
fi