#!/bin/sh

if [ -f "./env.sh" ]; then
  echo "env.sh file found"
  source ./env.sh
else
  echo "env.sh file not found"
fi

# Fail the script if certain environment variables are not set
check_variable() {
  if [ -z "$1" ]; then
    echo "Error: $2 is not set"
    exit 1
  fi
}

check_variable "$PROFILE" "PROFILE"
check_variable "$ENVIRONMENT" "ENVIRONMENT"
check_variable "$CI" "CI"
check_variable "$API_CODE" "API_CODE"
check_variable "$SERVICE_AUTH_PASSWORD_WASTE_ORGANISATION_BACKEND" "SERVICE_AUTH_PASSWORD_WASTE_ORGANISATION_BACKEND"

# Log the run_id and environment if CI is true
if [ "$CI" = "true" ]; then
  echo "\n\nrun_id: $RUN_ID in $ENVIRONMENT"
fi

# Get the current date and time
NOW=$(date +"%Y%m%d-%H%M%S")


# Define the directories for the test results
REPO_LOCATION=$(cd "$(dirname "$0")" && pwd)

JM_SCENARIOS=${REPO_LOCATION}/scenarios

JM_LOG_FOLDER=${REPO_LOCATION}/logs
JM_LOG_TEST=${JM_LOG_FOLDER}/jmeter-test
JM_LOG_REPORT=${JM_LOG_FOLDER}/jmeter-report

JM_RESULTS_FOLDER=${REPO_LOCATION}/results
JM_JTL_FILE=${JM_RESULTS_FOLDER}/results.jtl

JM_REPORT_FOLDER=${REPO_LOCATION}/reports

# Clean up previous test results and create fresh directories
for fileorFolder in ${JM_REPORT_FOLDER} ${JM_LOG_FOLDER} ${JM_RESULTS_FOLDER}; do
  if [ -f "$fileorFolder" ] || [ -d "$fileorFolder" ]; then
    rm -rf "$fileorFolder"
    mkdir -p "$fileorFolder"
  fi
done

# Ensure temp/ exists for bulk cumulative state (temp/bulk-upload-cumulative-state.txt); delete that file to reset the counter.
mkdir -p "${REPO_LOCATION}/temp"

# Build list of JMX files: PROFILE is one of external-api, bulk-upload, all, or a path to a .jmx under scenarios
echo "\n\nRunning profile: ${PROFILE}"
case "$PROFILE" in
  external-api)
    jmx_files=$(find "${JM_SCENARIOS}/create-waste-movement" "${JM_SCENARIOS}/update-waste-movement" -name "*.jmx" -type f 2>/dev/null | sort || true)
    ;;
  bulk-upload)
    jmx_files=$(find "${JM_SCENARIOS}/bulk-create-waste-movement" "${JM_SCENARIOS}/bulk-update-waste-movement" -name "*.jmx" -type f 2>/dev/null | sort || true)
    ;;
  all)
    jmx_files=$(find "${JM_SCENARIOS}" -name "*.jmx" -type f 2>/dev/null | sort || true)
    ;;
  *)
    if [ -f "${JM_SCENARIOS}/${PROFILE}" ]; then
      jmx_files="${JM_SCENARIOS}/${PROFILE}"
    else
      echo "Error: PROFILE='$PROFILE' is not supported (Supported profiles: external-api, bulk-upload, all, or a path to a JMX under scenarios e.g. bulk-update-waste-movement/successfully/baseline-test.jmx)."
      exit 1
    fi
    ;;
esac
if [ -z "$jmx_files" ]; then
  echo "No JMX files found for profile: $PROFILE"
  exit 1
fi

# Parse HTTP_PROXY if provided
if [ -n "$HTTP_PROXY" ]; then
  # Parse host and port (format: http://host:port)
  HTTP_PROXY_HOST=$(echo "$HTTP_PROXY" | cut -d: -f2 | cut -d/ -f3)
  HTTP_PROXY_PORT=$(echo "$HTTP_PROXY" | cut -d: -f3 | cut -d/ -f1)
  JM_COMMAND_LINE_PROXY_OPTION="-H${HTTP_PROXY_HOST} -P${HTTP_PROXY_PORT} -Jhttp.proxyHost=${HTTP_PROXY_HOST} -Jhttp.proxyPort=${HTTP_PROXY_PORT}"
  echo "Using HTTP_PROXY_HOST: $HTTP_PROXY_HOST"
  echo "Using HTTP_PROXY_PORT: $HTTP_PROXY_PORT"
else
  echo "No HTTP proxy configured"
  JM_COMMAND_LINE_PROXY_OPTION=""
fi

echo "Using JM_SCENARIOS: $JM_SCENARIOS"
echo "Using JM_REPORT_FOLDER: $JM_REPORT_FOLDER"
echo "Using JM_LOG_TEST: $JM_LOG_TEST"
echo "Using JM_JTL_FILE: $JM_JTL_FILE"
echo "Using CI: $CI"
echo "Using ENVIRONMENT: $ENVIRONMENT"
echo "Using PROFILE: $PROFILE"
echo "Using DEBUG: ${DEBUG:-false}"
echo "Using jmx_files: $jmx_files"

# Run all JMX files in scenarios folder (including subdirectories)
test_exit_code=0
for jmx_file in $jmx_files; do
  echo "\n\nRunning: $jmx_file\n\n"
  jmeter -n -t "$jmx_file" -l "${JM_JTL_FILE}" -j ${JM_LOG_TEST} \
    -Jenvironment=${ENVIRONMENT} \
    -Jci=${CI} \
    -JapiCode=${API_CODE} \
    -JserviceAuthPasswordWasteOrganisationBackend=${SERVICE_AUTH_PASSWORD_WASTE_ORGANISATION_BACKEND} \
    -JclientId=${COGNITO_CLIENT_ID:-} \
    -JclientSecret=${COGNITO_CLIENT_SECRET:-} \
    -JauthBaseUrl=${COGNITO_OAUTH_BASE_URL:-} \
    -JcdpApiKey=${CDP_API_KEY:-} \
    -Jdebug=${DEBUG:-false} \
    -Jresultcollector.action_if_file_exists=APPEND \
    -Jjmeter.save.saveservice.response_data=true \
    -Jjmeter.save.saveservice.response_message=true \
    -Jjmeter.save.saveservice.assertion_results_failure_message=true \
    -Jsample_variables=bulkMovementsInRequest,bulkMovementsCumulativeTotal,bulkMovementsPerSecond \
    ${JM_COMMAND_LINE_PROXY_OPTION} \
    -q ${REPO_LOCATION}/user.properties
  single_test_exit_code=$?
  if [ "$single_test_exit_code" -ne 0 ]; then
    echo "Error running: $(basename "$jmx_file"), error code: $single_test_exit_code"
    test_exit_code=1
  fi
done

# Generate report from combined results
echo "\n\nGenerating consolidated report..."
jmeter -g ${JM_JTL_FILE} -e -o ${JM_REPORT_FOLDER} -j ${JM_LOG_REPORT} -q ${REPO_LOCATION}/user.properties

# Bulk upload tests may write movement-count summary next to the JTL (see scripts/logging/bulk_upload_metrics_init.groovy)
if [ -f "${JM_RESULTS_FOLDER}/bulk-movement-metrics.txt" ]; then
  cp "${JM_RESULTS_FOLDER}/bulk-movement-metrics.txt" "${JM_REPORT_FOLDER}/"
fi

if [ "$CI" = "true" ]; then
  # Publish the results into S3 so they can be displayed in the CDP Portal
  if [ -n "$RESULTS_OUTPUT_S3_PATH" ]; then
    # Copy the JTL report file and the generated report files to the S3 bucket
    if [ -f "$JM_REPORT_FOLDER/index.html" ]; then
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_JTL_FILE" "$RESULTS_OUTPUT_S3_PATH/$(basename "$JM_JTL_FILE")"
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_LOG_TEST" "$RESULTS_OUTPUT_S3_PATH/$(basename "$JM_LOG_TEST")"
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_LOG_REPORT" "$RESULTS_OUTPUT_S3_PATH/$(basename "$JM_LOG_REPORT")"
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_REPORT_FOLDER" "$RESULTS_OUTPUT_S3_PATH" --recursive
        if [ $? -eq 0 ]; then
          echo "JTL report file and test results published to $RESULTS_OUTPUT_S3_PATH"
        fi
    else
        echo "$JM_REPORT_FOLDER/index.html is not found"
        exit 1
    fi
  else
    echo "RESULTS_OUTPUT_S3_PATH is not set"
    exit 1
  fi
elif [ "$CI" = "false" ]; then
  echo "All tests completed"
  if command -v open >/dev/null 2>&1; then
    echo "Opening report in browser..."
    open ${JM_REPORT_FOLDER}/index.html
  else
    echo "Report generated at: ${JM_REPORT_FOLDER}/index.html"
  fi
fi

exit $test_exit_code
