# digital-waste-tracking-perf-test

A JMeter-based performance testing suite for the Digital Waste Tracking API on the CDP Platform.

- [Running Tests](#running-tests)
- [Local Development](#local-development)
- [CDP Platform Execution](#cdp-platform-execution)
- [Development Guidelines](#development-guidelines)
- [Licence](#licence)

## Running Tests

### Using the Entrypoint Script

The repository provides an entrypoint script for running JMeter tests. **Important**: The script sources `env.sh` automatically, so you must set all environment variables in the `env.sh` file rather than exporting them in the command line.

```bash
# Run single test (uses TEST_SCENARIO from env.sh)
./entrypoint.sh

# Run all tests (set TEST_SCENARIO=all in env.sh)
./entrypoint.sh
```

### Test Execution Flow

1. **Cleanup**: Removes previous test results from `reports/`, `logs/`, `results/` directories
2. **Authentication**: Gets OAuth2 token using shared authentication script
3. **Test Execution**: Runs JMX files with proper environment variables
4. **Report Generation**: Creates HTML report from CSV results
5. **Output**: Opens report in browser (local) or uploads to S3 (CI)

## Local Development

### Quick Start

1. **Set up environment variables in `env.sh`**:
   ```bash
   cp env.sh.template env.sh
   # Edit env.sh with your credentials and test scenario
   ```

   Required variables to set in `env.sh`:
   - `ENVIRONMENT`: Target environment (dev, test, perf-test, prod)
   - `TEST_SCENARIO`: JMX file path or "all" for all tests
   - `CI`: Set to "false" for local development
   - `COGNITO_CLIENT_ID`: OAuth2 client ID
   - `COGNITO_CLIENT_SECRET`: OAuth2 client secret
   - `COGNITO_OAUTH_BASE_URL`: OAuth2 base URL
   - `ORGANISATION_API_ID`: Organization API ID

2. **Run tests locally**:

The repository provides an entrypoint script for running JMeter tests. **Important**: The script sources `env.sh` automatically, so you must set all environment variables in the `env.sh` file rather than exporting them in the command line.

   ```bash
   # Run specific test (uses TEST_SCENARIO from env.sh)
   ./entrypoint.sh
   
   # Or run all tests (set TEST_SCENARIO=all in env.sh)
   ./entrypoint.sh
   ```

### Directory Structure

- `scenarios/`: JMX test files organized by operation and test type
- `scripts/`: Groovy scripts for authentication and payload generation
- `reports/`: Generated HTML reports (gitignored)
- `logs/`: JMeter execution logs (gitignored)
- `results/`: CSV result files (gitignored)
- `temp/`: Temporary files generated during test execution (gitignored)

## CDP Platform Execution

Test suites are built automatically by the [.github/workflows/publish.yml](.github/workflows/publish.yml) action whenever changes are committed to the `main` branch.

The CDP Platform runs test suites as ECS tasks, automatically provisioning infrastructure as required. Results are published to the CDP Portal.

**⚠️ Important CDP Platform Limitation**: The CDP Platform has a **maximum runtime limit of 2 hours**. Test processes will be automatically killed if they exceed this limit. The current test pack duration is approximately 2 hours 22 minutes, so individual test runs should be executed rather than running the complete test suite in a single execution.

**Soak Testing Limitation**: Due to the 2-hour runtime limit, soak testing (long-duration tests to identify memory leaks, resource exhaustion, or performance degradation over time) cannot be performed in the CDP CI environment. Soak testing would need to be conducted in local environments or alternative platforms that support longer test durations.

## Local Testing with LocalStack

### Build a new Docker image
```
docker build . -t my-performance-tests
```
### Create a Localstack bucket
```
aws --endpoint-url=localhost:4566 s3 mb s3://my-bucket
```

### Run performance tests

```
docker run \
-e S3_ENDPOINT='http://host.docker.internal:4566' \
-e RESULTS_OUTPUT_S3_PATH='s3://my-bucket' \
-e AWS_ACCESS_KEY_ID='test' \
-e AWS_SECRET_ACCESS_KEY='test' \
-e AWS_SECRET_KEY='test' \
-e AWS_REGION='eu-west-2' \
my-performance-tests
```

docker run -e S3_ENDPOINT='http://host.docker.internal:4566' -e RESULTS_OUTPUT_S3_PATH='s3://cdp-infra-dev-test-results/cdp-portal-perf-tests/95a01432-8f47-40d2-8233-76514da2236a' -e AWS_ACCESS_KEY_ID='test' -e AWS_SECRET_ACCESS_KEY='test' -e AWS_SECRET_KEY='test' -e AWS_REGION='eu-west-2' -e ENVIRONMENT='perf-test' my-performance-tests

## Development Guidelines

This repository includes comprehensive cursor rules for LLM interactions and JMX file patterns. The rules are located in the `.cursor/rules/` directory and provide guidance for:

### Core Testing Principles
- **Performance Testing Focus**: All tests focus on performance metrics (load, stress, spike) rather than functional testing
- **Test Organization**: Tests organized by operation (`create-waste-movement`, `update-waste-movement`) and intensity level (`baseline`, `load`, `stress`, `spike`)
- **Shared Authentication**: Single OAuth2 token shared across all tests for efficiency
- **Dynamic Payload Generation**: Groovy scripts generate realistic test data with proper variable substitution

### Test Intensity Guidelines
- **Load Tests**: 30 users, 30 minutes duration, normal expected load
- **Stress Tests**: 50 users, 30 minutes duration, beyond normal capacity
- **Spike Tests**: 3-phase approach (5→50→5 users), tests system recovery capabilities
- **Baseline Tests**: 1 user, single iteration, functional validation

### Key Rule Files
- `development-workflow.mdc` - Guidelines for adding, modifying, and maintaining tests
- `jmx-structure.mdc` - Standards for JMX file organization and structure
- `jmx-patterns.mdc` - Detailed patterns and implementation guidelines
- `test-intensity.mdc` - Guidelines for load, stress, and spike testing
- `payload-management.mdc` - Rules for managing shared JSON payload files
- `environment-management.mdc` - Environment variable configuration and security
- `shared-authentication.mdc` - Shared authentication patterns and implementation
- `cdp-integration.mdc` - CDP Platform specific considerations

### Adding New Tests
1. Determine appropriate test category (load/stress/spike)
2. Create JMX file in correct folder structure
3. Use existing payload scripts with appropriate testType
4. Configure realistic user counts and durations
5. Test locally before committing
6. Update documentation


## Licence

THIS INFORMATION IS LICENSED UNDER THE CONDITIONS OF THE OPEN GOVERNMENT LICENCE found at:

<http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3>

The following attribution statement MUST be cited in your products and applications when using this information.

> Contains public sector information licensed under the Open Government licence v3

### About the licence

The Open Government Licence (OGL) was developed by the Controller of Her Majesty's Stationery Office (HMSO) to enable
information providers in the public sector to license the use and re-use of their information under a common open
licence.

It is designed to encourage use and re-use of information freely and flexibly, with only a few conditions.
