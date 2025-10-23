import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.util.Base64
import java.time.Duration

// Check if access token already exists in global properties
Long accessTokenCreatedAt = Long.parseLong(props.get("global_access_token_created_at") ?: "0")
Long now = System.currentTimeMillis()
Long accessTokenExpiresAt = accessTokenCreatedAt + (3600000 - 600000);  // 1 hour - 10 minutes = 50 minutes

if (props.get("global_access_token") == null || now > accessTokenExpiresAt) {
    log.info(props.get("global_access_token") == null ? "No access token found, authenticating..." : "Access token expired, re-authenticating...")

    // Get client credentials from JMeter properties
    String clientId = props.get("clientId")
    String clientSecret = props.get("clientSecret")
    String authBaseUrl = props.get("authBaseUrl")

    if (!clientId || !clientSecret || !authBaseUrl) {
        String errorMsg = "Missing required authentication properties: clientId, clientSecret, or authBaseUrl"
        log.error(errorMsg)
        println("ERROR: " + errorMsg)
        throw new Exception(errorMsg)
    }

    // Encode client credentials to Base64
    String credentials = "${clientId}:${clientSecret}"
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"))
    String basicAuthHeader = "Basic " + encoded

    // Prepare OAuth2 request
    String requestBody = "grant_type=client_credentials"
    String authUrl = "${authBaseUrl}/oauth2/token"

    // Create HTTP client with optional proxy configuration
    HttpClient.Builder clientBuilder = HttpClient.newBuilder()
    
    // Configure proxy if provided
    String proxyHost = props.get("http.proxyHost")
    String proxyPort = props.get("http.proxyPort")
    if (proxyHost && proxyPort && !proxyHost.isEmpty() && !proxyPort.isEmpty()) {
        clientBuilder.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))))
        log.info("Using HTTP proxy: ${proxyHost}:${proxyPort}")
    }
    
    HttpClient client = clientBuilder.build()
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(authUrl))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("Authorization", basicAuthHeader)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()

    // Send request and extract token
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        // Parse JSON response using JsonSlurper
        String responseBody = response.body()
        def jsonSlurper = new groovy.json.JsonSlurper()
        def jsonResponse = jsonSlurper.parseText(responseBody)
        
        String extractedToken = jsonResponse.access_token

        if (extractedToken && extractedToken.trim().length() > 0) {
            props.put("global_access_token", extractedToken.trim())
            props.put("global_access_token_created_at", String.valueOf(System.currentTimeMillis()))
            log.info("Successfully authenticated and stored global access token")
        } else {
            throw new Exception("Failed to extract access token from response")
        }
    } else {
        throw new Exception("Authentication failed with status: " + response.statusCode() + ", Response: " + response.body())
        // temp debugging line
        println("Authentication failed with status: " + response.statusCode() + ", Response: " + response.body())
    }
} else {
    log.info("Reusing existing global access token")
}