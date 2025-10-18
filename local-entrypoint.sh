#!/bin/bash

# Exit on error
set -e

# Load environment variables if present
if [ -f "./env.sh" ]; then
  echo "env.sh file found"
  source ./env.sh
else
  echo "env.sh file not found"
fi

# Clean up previous test results
for fileorFolder in ./reports ./results.jtl ./jmeter.log; do
  if [ -f "$fileorFolder" ] || [ -d "$fileorFolder" ]; then
    rm -rf "$fileorFolder"
  fi
done

echo "Running tests locally"

# Run all JMX files in scenarios folder (including subdirectories)
find scenarios -name "*.jmx" -type f | while read -r jmx_file; do
  echo "Running: $(basename "$jmx_file")"
  jmeter -n -t "$jmx_file" -l results.jtl -j jmeter.log \
    -Jenvironment=${ENVIRONMENT} \
    -JorganisationApiId=${ORGANISATION_API_ID} \
    -JclientId=${COGNITO_CLIENT_ID} \
    -JclientSecret=${COGNITO_CLIENT_SECRET} \
    -JauthBaseUrl=${COGNITO_OAUTH_BASE_URL} \
    -Jresultcollector.action_if_file_exists=APPEND 
done

# Generate report from combined results
echo "Generating consolidated report..."
jmeter -g results.jtl -e -o reports/ -j jmeter.log

echo "All tests completed"
if command -v open >/dev/null 2>&1; then
  echo "Opening report in browser..."
  open reports/index.html
else
  echo "Report generated at: reports/index.html"
fi

exit 0