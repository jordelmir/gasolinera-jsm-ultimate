#!/bin/bash
# Configuration Drift Detection Script
# This script checks for inconsistencies in the OpenAPI client generation configuration

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Configuration Drift Detection${NC}"
echo "=================================="
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

# Expected services from the registry
EXPECTED_SERVICES=("auth-service" "station-service" "coupon-service" "redemption-service" "ad-engine" "raffle-service")
DRIFT_DETECTED=false

# Check service registry consistency
check_service_registry() {
    print_status "info" "Checking service registry consistency..."

    # Verify all expected services have corresponding directories
    for service in "${EXPECTED_SERVICES[@]}"; do
        if [ ! -d "services/$service" ]; then
            print_status "error" "Service directory missing: services/$service"
            DRIFT_DETECTED=true
        else
            print_status "success" "Service directory exists: services/$service"
        fi
    done

    # Check for services not in expected list
    for service_dir in services/*/; do
        if [ -d "$service_dir" ]; then
            service_name=$(basename "$service_dir")

            # Skip if it's in expected services
            if [[ " ${EXPECTED_SERVICES[@]} " =~ " ${service_name} " ]]; then
                continue
            fi

            # Check if it's in the build.gradle.kts registry
            if grep -q "$service_name" build.gradle.kts; then
                print_status "warning" "Service in registry but not in expected list: $service_name"
            else
                print_status "warning" "Service directory exists but not in registry: $service_name"
                DRIFT_DETECTED=true
            fi
        fi
    done
}

# Check OpenAPI specifications
check_openapi_specs() {
    print_status "info" "Checking OpenAPI specifications..."

    for service in "${EXPECTED_SERVICES[@]}"; do
        local spec_file="services/$service/openapi.yaml"

        if [ ! -f "$spec_file" ]; then
            print_status "warning" "OpenAPI spec missing: $spec_file"
        else
            # Check if spec is valid YAML
            if python3 -c "import yaml; yaml.safe_load(open('$spec_file'))" 2>/dev/null; then
                print_status "success" "Valid OpenAPI spec: $service"
            else
                print_status "error" "Invalid YAML in OpenAPI spec: $spec_file"
                DRIFT_DETECTED=true
            fi

            # Check spec size (detect placeholder specs)
            local file_size=$(wc -c < "$spec_file" 2>/dev/null || echo "0")
            if [ "$file_size" -lt 500 ]; then
                print_status "warning" "OpenAPI spec seems minimal (${file_size} bytes): $service"
            fi
        fi
    done
}

# Check build configuration
check_build_configuration() {
    print_status "info" "Checking build configuration..."

    # Check if serviceRegistry exists in build.gradle.kts
    if grep -q "val serviceRegistry" build.gradle.kts; then
        print_status "success" "Service registry found in build.gradle.kts"
    else
        print_status "error" "Service registry missing in build.gradle.kts"
        DRIFT_DETECTED=true
    fi

    # Check if all expected services are in the registry
    for service in "${EXPECTED_SERVICES[@]}"; do
        if grep -q "servicePath = \"$service\"" build.gradle.kts; then
            print_status "success" "Service in registry: $service"
        else
            print_status "error" "Service missing from registry: $service"
            DRIFT_DETECTED=true
        fi
    done

    # Check for required plugins
    local required_plugins=("org.openapi.generator" "org.springdoc.openapi-gradle-plugin")
    for plugin in "${required_plugins[@]}"; do
        if grep -q "$plugin" build.gradle.kts; then
            print_status "success" "Required plugin found: $plugin"
        else
            print_status "error" "Required plugin missing: $plugin"
            DRIFT_DETECTED=true
        fi
    done
}

# Check generated clients
check_generated_clients() {
    print_status "info" "Checking generated clients..."

    local build_dir="build/generated"

    if [ ! -d "$build_dir" ]; then
        print_status "warning" "Generated clients directory not found: $build_dir"
        return
    fi

    for service in "${EXPECTED_SERVICES[@]}"; do
        local client_dir="$build_dir/$service-client"

        if [ ! -d "$client_dir" ]; then
            print_status "warning" "Generated client missing: $service"
        else
            # Check if client has source files
            local source_files=$(find "$client_dir" -name "*.kt" -o -name "*.java" | wc -l)
            if [ "$source_files" -gt 0 ]; then
                print_status "success" "Generated client has $source_files source files: $service"
            else
                print_status "warning" "Generated client has no source files: $service"
            fi
        fi
    done
}

# Check service build configurations
check_service_builds() {
    print_status "info" "Checking service build configurations..."

    for service in "${EXPECTED_SERVICES[@]}"; do
        local build_file="services/$service/build.gradle.kts"

        if [ ! -f "$build_file" ]; then
            print_status "warning" "Build file missing: $build_file"
            continue
        fi

        # Check for SpringDoc plugi   if grep -q "org.springdoc.openapi-gradle-plugin" "$build_file"; then
            print_status "success" "SpringDoc plugin configured: $service"
        else
            print_status "warning" "SpringDoc plugin missing: $service"
        fi

        # Check for SpringDoc dependency
        if grep -q "springdoc-openapi-starter-webmvc-ui" "$build_file"; then
            print_status "success" "SpringDoc dependency configured: $service"
        else
            print_status "warning" "SpringDoc dependency missing: $service"
        fi
    done
}

# Check package structure consistency
check_package_structure() {
    print_status "info" "Checking package structure consistency..."

    # Expected package pattern: com.gasolinerajsm.sdk.{service}
    for service in "${EXPECTED_SERVICES[@]}"; do
        local service_clean=$(echo "$service" | sed 's/-service//' | sed 's/-engine//' | tr '-' '')
        local expected_package="com.gasolinerajsm.sdk.$service_clean"

        if grep -q "sdkPackage = \"$expected_package\"" build.gradle.kts; then
            print_status "success" "Package structure consistent: $service -> $expected_package"
        else
            print_status "warning" "Package structure inconsistent for: $service"
        fi
    done
}

# Check port assignments
check_port_assignments() {
    print_status "info" "Checking port assignments..."

    # Expected ports
    declare -A expected_ports=(
        ["auth-service"]="8081"
        ["station-service"]="8083"
        ["coupon-service"]="8086"
        ["redemption-service"]="8082"
        ["ad-engine"]="8084"
        ["raffle-service"]="8085"
    )

    for service in "${EXPECTED_SERVICES[@]}"; do
        local expected_port="${expected_ports[$service]}"

        if grep -q "port = $expected_port" build.gradle.kts; then
            print_status "success" "Port assignment correct: $service -> $expected_port"
        else
            print_status "warning" "Port assignment inconsistent for: $service (expected: $expected_port)"
        fi
    done
}

# Generate drift report
generate_drift_report() {
    local report_file="configuration-drift-report.md"

    cat > "$report_file" << EOF
# Configuration Drift Report

Generated on: $(date)

## Summary

Configuration drift detection completed with the following results:

EOF

    if [ "$DRIFT_DETECTED" = true ]; then
        cat >> "$report_file" << EOF
⚠️ **DRIFT DETECTED**: Configuration inconsistencies found.

## Issues Found

EOF
    else
        cat >> "$report_file" << EOF
✅ **NO DRIFT DETECTED**: All configurations are consistent.

EOF
    fi

    cat >> "$report_file" << EOF
## Checked Components

- [x] Service registry consistency
- [x] OpenAPI specifications
- [x] Build configuration
- [x] Generated clients
- [x] Service build files
- [x] Package structure
- [x] Port assignments

## Expected Services

EOF

    for service in "${EXPECTED_SERVICES[@]}"; do
        echo "- $service" >> "$report_file"
    done

    cat >> "$report_file" << EOF

## Recommendations

EOF

    if [ "$DRIFT_DETECTED" = true ]; then
        cat >> "$report_file" << EOF
1. Review the issues listed above
2. Update configurations to maintain consistency
3. Regenerate clients if necessary: \`./gradlew generateAllClients\`
4. Run verification: \`./scripts/verify-client-generation.sh\`
5. Re-run drift detection to confirm fixes

EOF
    else
        cat >> "$report_file" << EOF
1. Continue regular monitoring
2. Run drift detection weekly
3. Update this script when adding new services
4. Maintain documentation consistency

EOF
    fi

    cat >> "$report_file" << EOF
## Next Steps

- Schedule regular drift detection (weekly)
- Update expected services list when adding new services
- Integrate drift detection into CI/CD pipeline
- Monitor for configuration changes in pull requests

---
*Generated by configuration drift detection script*
EOF

    print_status "success" "Drift report generated: $report_file"
}

# Main execution
main() {
    print_status "info" "Starting configuration drift detection..."
    echo ""

    check_service_registry
    echo ""

    check_openapi_specs
    echo ""

    check_build_configuration
    echo ""

    check_generated_clients
    echo ""

    check_service_builds
    echo ""

    check_package_structure
    echo ""

    check_port_assignments
    echo ""

    generate_drift_report
    echo ""

    # Final summary
    echo -e "${BLUE}Final Summary${NC}"
    echo "============="

    if [ "$DRIFT_DETECTED" = true ]; then
        print_status "error" "Configuration drift detected!"
        print_status "info" "Review the issues above and the generated report."
        print_status "info" "Run './scripts/verify-client-generation.sh' after fixing issues."
        exit 1
    else
        print_status "success" "No configuration drift detected!"
        print_status "info" "All configurations are consistent."
        print_status "info" "System is ready for client generation."
    fi
}

# Check prerequisites
if [ ! -f "./gradlew" ]; then
    print_status "error" "Gradle wrapper not found. Please run this script from the project root."
    exit 1
fi

if [ ! -f "build.gradle.kts" ]; then
    print_status "error" "build.gradle.kts not found. Please run this script from the project root."
    exit 1
fi

# Run main function
main "$@"