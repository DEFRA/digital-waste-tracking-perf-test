# JMeter Scripts

This directory contains reusable Groovy scripts for JMeter performance tests.

## Scripts

### `encode_credentials.groovy`
Encodes client credentials for OAuth2 Basic authentication.

**Usage:**
- Used in JSR223 PreProcessor
- Reads `clientId` and `clientSecret` variables
- Creates `basic_auth_header` variable with Base64-encoded credentials

**Variables:**
- Input: `clientId`, `clientSecret`
- Output: `basic_auth_header`

**Example:**
```groovy
// Input variables
clientId = "your-client-id"
clientSecret = "your-client-secret"

// Output variable
basic_auth_header = "Basic eW91ci1jbGllbnQtaWQ6eW91ci1jbGllbnQtc2VjcmV0"
```

## Benefits of External Scripts

1. **Reusability**: Can be used across multiple test plans
2. **Maintainability**: Easy to update without modifying JMX files
3. **Version Control**: Scripts can be tracked separately
4. **Testing**: Scripts can be unit tested independently
5. **Readability**: Cleaner JMX files without embedded code
