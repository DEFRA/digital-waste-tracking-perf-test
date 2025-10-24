import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult
import org.apache.jmeter.protocol.http.util.HTTPConstants
import org.apache.jmeter.protocol.http.control.HeaderManager
import org.apache.jmeter.protocol.http.control.Header
import org.apache.jmeter.protocol.http.util.HTTPArgument
import java.util.Base64

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

    // Create JMeter HTTP sampler
    HTTPSamplerProxy sampler = new HTTPSamplerProxy()
    sampler.setDomain(authBaseUrl.replace("https://", "").replace("http://", ""))
    sampler.setProtocol("https")
    sampler.setPath("/oauth2/token")
    sampler.setMethod(HTTPConstants.POST)
    sampler.setPostBodyRaw(true)
    
    // Configure proxy based on environment
    String proxyHost = props.get("http.proxyHost")
    String proxyPort = props.get("http.proxyPort")
    if (proxyHost && !proxyHost.isEmpty() && proxyPort && !proxyPort.isEmpty()) {
        sampler.setProxyHost(proxyHost)
        sampler.setProxyPortInt(proxyPort)
        log.info("Using proxy: ${proxyHost}:${proxyPort}")
    } else {
        // No proxy configured for local execution
        sampler.setProxyHost("")
        sampler.setProxyPortInt("0")
        log.info("No proxy configured - using direct connection")
    }
    
    // Create header manager
    HeaderManager headerManager = new HeaderManager()
    headerManager.add(new Header("Content-Type", "application/x-www-form-urlencoded"))
    headerManager.add(new Header("Authorization", basicAuthHeader))
    sampler.setHeaderManager(headerManager)
    
    // Set the request body using addNonEncodedArgument
    sampler.addNonEncodedArgument("", requestBody, "")
    
    // Proxy is automatically configured via JMeter command line options
    
    // Set timeouts
    sampler.setConnectTimeout("30000")
    sampler.setResponseTimeout("30000")

    // Execute request
    HTTPSampleResult result = sampler.sample()
    
    if (result.getResponseCode() == "200") {
        // Parse JSON response using JsonSlurper
        def jsonSlurper = new groovy.json.JsonSlurper()
        def jsonResponse = jsonSlurper.parseText(result.getResponseDataAsString())
        
        String extractedToken = jsonResponse.access_token

        if (extractedToken && extractedToken.trim().length() > 0) {
            props.put("global_access_token", extractedToken.trim())
            props.put("global_access_token_created_at", String.valueOf(System.currentTimeMillis()))
            log.info("Successfully authenticated and stored global access token")
        } else {
            throw new Exception("Failed to extract access token from response")
        }
    } else {
        log.error("Authentication failed with status: " + result.getResponseCode() + ", Response: " + result.getResponseDataAsString())
    }
} else {
    log.info("Reusing existing global access token")
}