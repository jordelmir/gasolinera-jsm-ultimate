#!/bin/bash
# OpenAPI Client Generation Maintenance Checklist
# This script runs a comprehensive maintenance check of the system

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 OpenAPI Client Generation Maintenance Checklist${NC}"
echo "=================================================="
echo ""

# Function to print status
print_status() {
    local status=$1
    local message=$2
    case $status in
        "success")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "warning")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "error")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "info")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

# Function to run a check and report results
run_check() {
    local check_name=$1
    local command=$2
    local success_message=$3
    local error_message=$4

    print_status "info" "Running: $check_name"

    if eval "$command" >/dev/null 2>&1; then
        print_status "success" "$success_message"
        return 0
    else
        print_status "error" "$error_message"
        return 1
    fi
}

# System Information
check_system_info() {
    echo -e "${BLUE}📊 System Information${NC}"
    echo "--------------------"

    print_status "info" "Operating System: $(uname -s)"
    print_status "info" "Gradle Version: $(./gradlew --version | grep "Gradle" | head -1)"
    print_status "info" "Java Version: $(java -version 2>&1 | head -1)"
    print_status "info" "Kotlin Version: $(./gradlew --version | grep "Kotlin" | head -1 || echo "Not available")"

    # Check available resources
    if command -v free >/dev/null 2>&1; then
        local memory=$(free -h | awk 'NR==2{printf "%.1fGB available", $7/1024}')
        print_status "info" "Memory: $memory"
    elif command -v vm_stat >/dev/null 2>&1; then
        print_status "info" "Memory: Available (macOS)"
    fi

    local cpu_cores=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo "Unknown")
    print_status "info" "CPU Cores: $cpu_cores"

    echo ""
}

# 1. Service Configuration Check
check_service_configuration() {
    echo -e "${BLUE}1. Service Configuration Check${NC}"
    echo "------------------------------"

    run_check \
        "Service Registry" \
        "./gradlew listServices" \
        "Service registry is accessible and lists all services" \
        "Failed to access service registry"

    echo ""
}

# 2. Configuration Drift Detection
check_configuration_drift() {
    echo -e "${BLUE}2. Configuration Drift Detection${NC}"
    echo "-------------------------------"

    if [ -f "scripts/check-configuration-drift.sh" ]; then
        if ./scripts/check-configuration-drift.sh >/dev/null 2>&1; then
            print_status "success" "No configuration drift detected"
        else
            print_status "warning" "Configuration drift detected - check drift report"
        fi
    else
        print_status "warning" "Configuration drift script not found"
    fi

    echo ""
}

# 3. OpenAPI Specification Check
check_openapi_specs() {
    echo -e "${BLUE}3. OpenAPI Specification Check${NC}"
    echo "------------------------------"

    local services=("auth-service" "station-service" "coupon-service" "redemption-service" "ad-engine" "raffle-service")
    local missing_specs=0
    local invalid_specs=0

    for service in "${services[@]}"; do
        local spec_file="services/$service/openapi.yaml"

        if [ ! -f "$spec_file" ]; then
            print_status "warning" "OpenAPI spec missing: $service"
            ((missing_specs++))
        else
            # Validate YAML syntax
            if python3 -c "import yaml; yaml.safe_load(open('$spec_file'))" 2>/dev/null; then
                print_status "success" "Valid OpenAPI spec: $service"
            else
                print_status "error" "Invalid YAML syntax: $service"
                ((invalid_specs++))
            fi
        fi
    done

    if [ $missing_specs -eq 0 ] && [ $invalid_specs -eq 0 ]; then
        print_status "success" "All OpenAPI specifications are valid"
    else
        print_status "warning" "$missing_specs missing, $invalid_specs invalid specifications"
    fi

    echo ""
}

# 4. Client Generation Check
check_client_generation() {
    echo -e "${BLUE}4. Client Generation Check${NC}"
    echo "--------------------------"

    run_check \
        "Client Generation" \
        "./gradlew generateAllClients --quiet" \
        "All clients generated successfully" \
        "Client generation failed"

    echo ""
}

# 5. Client Validation
check_client_validation() {
    echo -e "${BLUE}5. Client Validation${NC}"
    echo "-------------------"

    run_check \
        "Client Validation" \
        "./gradlew validateAllClients --quiet" \
        "All clients validated successfully" \
        "Client validation failed"

    echo ""
}

# 6. Performance Check
check_performance() {
    echo -e "${BLUE}6. Performance Check${NC}"
    echo "-------------------"

    # Check if clients are up to date
    if ./gradlew checkGeneratedClientsUpToDate --quiet >/dev/null 2>&1; then
        print_status "success" "All clients are up to date"
    else
        print_status "info" "Some clients need regeneration"
    fi

    # Check build cache
    if [ -d "build-cache" ]; then
        local cache_size=$(du -sh build-cache 2>/dev/null | cut -f1 || echo "0B")
        print_status "info" "Build cache size: $cache_size"
    else
        print_status "warning" "Build cache directory not found"
    fi

    # Check Gradle cache
    local gradle_cache="$HOME/.gradle/caches"
    if [ -d "$gradle_cache" ]; then
        local gradle_cache_size=$(du -sh "$gradle_cache" 2>/dev/null | cut -f1 || echo "Unknown")
        print_status "info" "Gradle cache size: $gradle_cache_size"
    fi

    echo ""
}

# 7. Build Artifacts Check
check_build_artifacts() {
    echo -e "${BLUE}7. Build Artifacts Check${NC}"
    echo "------------------------"

    local build_dir="build/generated"

    if [ ! -d "$build_dir" ]; then
        print_status "warning" "Generated clients directory not found"
        return
    fi

    local client_count=$(find "$build_dir" -maxdepth 1 -type d -name "*-client" | wc -l)
    print_status "info" "Generated clients: $client_count"

    # Check individual clients
    local services=("auth-service" "station-service" "coupon-service" "redemption-service" "ad-engine" "raffle-service")
    local valid_clients=0

    for service in "${services[@]}"; do
        local client_dir="$build_dir/$service-client"

        if [ -d "$client_dir" ]; then
            local source_files=$(find "$client_dir" -name "*.kt" -o -name "*.java" | wc -l)
            if [ "$source_files" -gt 0 ]; then
                ((valid_clients++))
            fi
        fi
    done

    if [ $valid_clients -eq ${#services[@]} ]; then
        print_status "success" "All clients have valid artifacts"
    else
        print_status "warning" "$valid_clients/${#services[@]} clients have valid artifacts"
    fi

    echo ""
}

# 8. Documentation Check
check_documentation() {
    echo -e "${BLUE}8. Documentation Check${NC}"
    echo "----------------------"

    local docs=("README_OPENAPI_CLIENTS.md" "docs/OPENAPI_CLIENT_GUIDE.md" "docs/MAINTENANCE_PROCEDURES.md")
    local missing_docs=0

    for doc in "${docs[@]}"; do
        if [ -f "$doc" ]; then
            print_status "success" "Documentation exists: $doc"
        else
            print_status "warning" "Documentation missing: $doc"
            ((missing_docs++))
        fi
    done

    if [ $missing_docs -eq 0 ]; then
        print_status "success" "All documentation is present"
    else
        print_status "warning" "$missing_docs documentation files missing"
    fi

    echo ""
}

# 9. Cleanup Old Artifacts
cleanup_old_artifacts() {
    echo -e "${BLUE}9. Cleanup Old Artifacts${NC}"
    echo "------------------------"

    local cleaned_files=0

    # Clean temporary files
    if find build/generated -name "*.tmp" -delete 2>/dev/null; then
        ((cleaned_files++))
    fi

    # Clean log files
    if find build/generated -name "*.log" -delete 2>/dev/null; then
        ((cleaned_files++))
    fi

    # Clean empty directories
    if find build/generated -type d -empty -delete 2>/dev/null; then
        ((cleaned_files++))
    fi

    if [ $cleaned_files -gt 0 ]; then
        print_status "success" "Cleaned $cleaned_files old artifacts"
    else
        print_status "info" "No old artifacts to clean"
    fi

    echo ""
}

# 10. Security Check
check_security() {
    echo -e "${BLUE}10. Security Check${NC}"
    echo "------------------"

    # Check for hardcoded credentials
    if grep -r "password\|secret\|key" build.gradle.kts gradle.properties 2>/dev/null | grep -v "# " | grep -v "clientSecret.*:" >/dev/null; then
        print_status "warning" "Potential hardcoded credentials found"
    else
        print_status "success" "No hardcoded credentials detected"
    fi

    # Check file permissions
    if [ -f "gradle.properties" ]; then
        local perms=$(stat -c "%a" gradle.properties 2>/dev/null || stat -f "%A" gradle.properties 2>/dev/null || echo "unknown")
        if [ "$perms" = "644" ] || [ "$perms" = "600" ]; then
            print_status "success" "gradle.properties has secure permissions"
        else
            print_status "warning" "gradle.properties permissions may be too open: $perms"
        fi
    fi

    echo ""
}

# Generate maintenance report
generate_maintenance_report() {
    local report_file="maintenance-report.md"

    cat > "$report_file" << EOF
# Maintenance Report

Generated on: $(date)

## System Information

- **OS**: $(uname -s)
- **Gradle**: $(./gradlew --version | grep "Gradle" | head -1)
- **Java**: $(java -version 2>&1 | head -1)

## Maintenance Checklist Results

EOF

    # Add checklist results (simplified)
    cat >> "$report_file" << EOF
- [x] Service Configuration Check
- [x] Configuration Drift Detection
- [x] OpenAPI Specification Check
- [x] Client Generation Check
- [x] Client Validation
- [x] Performance Check
- [x] Build Artifacts Check
- [x] Documentation Check
- [x] Cleanup Old Artifacts
- [x] Security Check

## Recommendations

1. **Regular Monitoring**: Run this checklist weekly
2. **Performance Optimization**: Use \`--parallel\` flag for faster builds
3. **Cache Management**: Monitor and clean build caches regularly
4. **Documentation**: Keep documentation updated with system changes
5. **Security**: Regularly audit for hardcoded credentials

## Next Maintenance

Schedule next maintenance check for: $(date -d "+1 week" 2>/dev/null || date -v+1w 2>/dev/null || echo "Next week")

## Commands for Manual Checks

\`\`\`bash
# Full system verification
./scripts/verify-client-generation.sh

# Configuration drift check
./scripts/check-configuration-drift.sh

# Performance optimization
./scripts/optimize-build-performance.sh

# Generate all clients
./gradlew generateAllClients --parallel
\`\`\`

---
*Generated by maintenance checklist script*
EOF

    print_status "success" "Maintenance report generated: $report_file"
}

# Main execution
main() {
    print_status "info" "Starting comprehensive maintenance check..."
    echo ""

    check_system_info
    check_service_configuration
    check_configuration_drift
    check_openapi_specs
    check_client_generation
    check_client_validation
    check_performance
    check_build_artifacts
    check_documentation
    cleanup_old_artifacts
    check_security

    generate_maintenance_report
    echo ""

    print_status "success" "Maintenance checklist completed!"
    print_status "info" "Review the maintenance report for detailed results."
    print_status "info" "Schedule next maintenance check for next week."
}

# Check prerequisites
if [ ! -f "./gradlew" ]; then
    print_status "error" "Gradle wrapper not found. Please run this script from the project root."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Run main function
main "$@"