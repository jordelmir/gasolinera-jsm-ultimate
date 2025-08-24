#!/bin/bash
# OpenAPI Client Generation Verification Script
# This script validates that all OpenAPI clients can be generated successfully

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICES=("auth-service" "station-service" "coupon-service" "redemption-service" "ad-engine" "raffle-service")
SERVICE_NAMES=("Auth" "Station" "Coupon" "Redemption" "AdEngine" "Raffle")
BUILD_DIR="build/generated"
REPORT_FILE="client-generation-report.md"

echo -e "${BLUE}🚀 OpenAPI Client Generation Verification${NC}"
echo "=============================================="
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

# Function to validate generated client
validate_generated_client() {
    local service=$1
    local service_name=$2
    local client_dir="$BUILD_DIR/$service-client"

    if [ ! -d "$client_dir" ]; then
        print_status "error" "Client directory not found: $client_dir"
        return 1
    fi

    print_status "success" "Client directory exists: $client_dir"

    # Check for source directory
    if [ -d "$client_dir/src" ]; then
        print_status "success" "Source directory exists in $service client"
    else
        print_status "error" "Source directory missing in $service client"
        return 1
    fi

    # Check for generated files
    local generated_files=$(find "$client_dir" -name "*.kt" -o -name "*.java" | wc -l)
    if [ "$generated_files" -gt 0 ]; then
        print_status "success" "Generated $generated_files source files for $service client"
    else
        print_status "error" "No source files generated for $service client"
        return 1
    fi

    return 0
}

# Function to generate report
generate_report() {
    local total_services=${#SERVICES[@]}
    local successful_services=$1
    local failed_services=$2

    cat > "$REPORT_FILE" << EOF
# OpenAPI Client Generation Report

Generated on: $(date)

## Summary

- **Total Services**: $total_services
- **Successful**: $successful_services
- **Failed**: $failed_services
- **Success Rate**: $(( successful_services * 100 / total_services ))%

## Services Status

EOF

    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local service_name="${SERVICE_NAMES[$i]}"
        local client_dir="$BUILD_DIR/$service-client"

        echo "### $service_name Service" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"

        if [ -d "$client_dir" ]; then
            echo "- ✅ **Status**: Client generated successfully" >> "$REPORT_FILE"
            echo "- 📁 **Location**: \`$client_dir\`" >> "$REPORT_FILE"

            local generated_files=$(find "$client_dir" -name "*.kt" -o -name "*.java" | wc -l)
            echo "- 📄 **Generated Files**: $generated_files" >> "$REPORT_FILE"

            # List some example files
            echo "- 📝 **Example Files**:" >> "$REPORT_FILE"
            find "$client_dir" -name "*.kt" -o -name "*.java" | head -5 | while read -r file; do
                echo "  - \`$(basename "$file")\`" >> "$REPORT_FILE"
            done
        else
            echo "- ❌ **Status**: Client generation failed" >> "$REPORT_FILE"
        fi

        echo "" >> "$REPORT_FILE"
    done

    print_status "success" "Report generated: $REPORT_FILE"
}

# Main execution
main() {
    local successful_services=0
    local failed_services=0

    print_status "info" "Starting OpenAPI client generation verification..."
    echo ""

    # Check root build configuration
    print_status "info" "Checking root build configuration..."
    if grep -q "serviceRegistry" build.gradle.kts; then
        print_status "success" "Service registry found in root build.gradle.kts"
    else
        print_status "error" "Service registry missing in root build.gradle.kts"
        exit 1
    fi

    echo ""

    # Generate all clients
    print_status "info" "Generating all OpenAPI clients..."
    if ./gradlew generateAllClients --quiet; then
        print_status "success" "All clients generated successfully"
    else
        print_status "error" "Failed to generate clients"
        exit 1
    fi

    echo ""

    # Process each service
    for i in "${!SERVICES[@]}"; do
        local service="${SERVICES[$i]}"
        local service_name="${SERVICE_NAMES[$i]}"

        echo -e "${BLUE}Validating $service_name Service ($service)${NC}"
        echo "----------------------------------------"

        if validate_generated_client "$service" "$service_name"; then
            ((successful_services++))
            print_status "success" "$service_name client validation passed"
        else
            ((failed_services++))
            print_status "error" "$service_name client validation failed"
        fi

        echo ""
    done

    # Generate summary report
    echo -e "${BLUE}Generating Report${NC}"
    echo "------------------"
    generate_report "$successful_services" "$failed_services"
    echo ""

    # Final summary
    echo -e "${BLUE}Final Summary${NC}"
    echo "============="
    print_status "info" "Total services processed: ${#SERVICES[@]}"
    print_status "success" "Successful: $successful_services"

    if [ "$failed_services" -gt 0 ]; then
        print_status "error" "Failed: $failed_services"
        echo ""
        print_status "warning" "Some services failed. Check the output above for details."
        exit 1
    else
        print_status "success" "All services completed successfully!"
        echo ""
        print_status "info" "You can now use the generated clients in your applications."
        print_status "info" "Check $REPORT_FILE for detailed information."
    fi
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