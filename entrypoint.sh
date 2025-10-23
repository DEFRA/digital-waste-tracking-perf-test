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

check_variable "$ENVIRONMENT" "ENVIRONMENT"
check_variable "$TEST_SCENARIO" "TEST_SCENARIO"
check_variable "$CI" "CI"
check_variable "$COGNITO_CLIENT_ID" "COGNITO_CLIENT_ID"
check_variable "$COGNITO_CLIENT_SECRET" "COGNITO_CLIENT_SECRET"
check_variable "$COGNITO_OAUTH_BASE_URL" "COGNITO_OAUTH_BASE_URL"
check_variable "$ORGANISATION_API_ID" "ORGANISATION_API_ID"

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
JM_LOG_FILE=${JM_LOG_FOLDER}/jmeter

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

# Build list of JMX files to run
if [ "${TEST_SCENARIO}" = "all" ]; then
  echo "\n\nRunning all scenarios"
  # Build list of all JMX files in scenarios folder (including subdirectories)
  jmx_files=$(find scenarios -name "*.jmx" -type f 2>/dev/null || echo "")
  if [ -z "$jmx_files" ]; then
    echo "No JMX files found in scenarios directory"
    exit 1
  fi
else
  echo "\n\nRunning scenario: ${TEST_SCENARIO}"
  SCENARIOFILE=${JM_SCENARIOS}/${TEST_SCENARIO}
  jmx_files="${SCENARIOFILE}"
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
echo "Using JM_LOG_FILE: $JM_LOG_FILE"
echo "Using JM_JTL_FILE: $JM_JTL_FILE"
echo "Using CI: $CI"
echo "Using ENVIRONMENT: $ENVIRONMENT"


# Run all JMX files in scenarios folder (including subdirectories)
test_exit_code=0
for jmx_file in $jmx_files; do
  echo "\n\nRunning: $jmx_file\n\n"
  jmeter -n -t "$jmx_file" -l "${JM_JTL_FILE}" -j ${JM_LOG_FILE} \
    -Jenvironment=${ENVIRONMENT} \
    -JorganisationApiId=${ORGANISATION_API_ID} \
    -JclientId=${COGNITO_CLIENT_ID} \
    -JclientSecret=${COGNITO_CLIENT_SECRET} \
    -JauthBaseUrl=${COGNITO_OAUTH_BASE_URL} \
    -Jresultcollector.action_if_file_exists=APPEND \
    ${JM_COMMAND_LINE_PROXY_OPTION}
  single_test_exit_code=$?
  if [ "$single_test_exit_code" -ne 0 ]; then
    echo "Error running: $(basename "$jmx_file"), error code: $single_test_exit_code"
    test_exit_code=1
  fi
done

# Generate report from combined results
echo "\n\nGenerating consolidated report..."
jmeter -g ${JM_JTL_FILE} -e -o ${JM_REPORT_FOLDER} -j ${JM_LOG_FILE} 

if [ "$CI" = "true" ]; then
  # Publish the results into S3 so they can be displayed in the CDP Portal
  if [ -n "$RESULTS_OUTPUT_S3_PATH" ]; then
    # Copy the JTL report file and the generated report files to the S3 bucket
    if [ -f "$JM_REPORT_FOLDER/index.html" ]; then
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_JTL_FILE" "$RESULTS_OUTPUT_S3_PATH/$(basename "$JM_JTL_FILE")"
        aws --endpoint-url=$S3_ENDPOINT s3 cp "$JM_LOG_FILE" "$RESULTS_OUTPUT_S3_PATH/$(basename "$JM_LOG_FILE")"
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
