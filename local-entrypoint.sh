#!/bin/bash

set -e

echo "Running tests locally"

source ./env.sh

if [ -f "reports/index.html" ]; then
  rm -rf reports/
fi

if [ -f "results.jtl" ]; then
  rm -rf results.jtl
fi

jmeter -n -t scenarios/create-waste-movement/successfully/baseline-test.jmx -l results.jtl -e -o reports/ \
  -Jenvironment=${ENVIRONMENT} \
  -JorganisationApiId=${ORGANISATION_API_ID} \
  -JclientId=${COGNITO_CLIENT_ID} \
  -JclientSecret=${COGNITO_CLIENT_SECRET} \
  -JauthBaseUrl=${COGNITO_OAUTH_BASE_URL} \
  -Jjmeter.save.saveservice.response_data=true \
  -Jjmeter.save.saveservice.samplerData=true \
  -Jjmeter.save.saveservice.requestHeaders=true \
  -Jjmeter.save.saveservice.responseHeaders=true \
  -Jjmeter.save.saveservice.assertions=true \
  -Jjmeter.save.saveservice.subresults=true \
  -Jjmeter.save.saveservice.bytes=true \
  -Jjmeter.save.saveservice.latency=true \
  -Jjmeter.save.saveservice.connect_time=true \
  -Jjmeter.save.saveservice.thread_counts=true \
  -Jjmeter.save.saveservice.timestamp_format=ms \
  -Jjmeter.save.saveservice.print_field_names=true \
  -Jjmeter.save.saveservice.response_data=true \
  -Jjmeter.save.saveservice.samplerData=true \
  -Jjmeter.save.saveservice.requestHeaders=true \
  -Jjmeter.save.saveservice.responseHeaders=true \

echo "Tests completed"

open reports/index.html

exit 0