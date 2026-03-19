/**
 * Derives waste movement external API (public API) URL components from ENVIRONMENT.
 *
 * - ENVIRONMENT=local: http://localhost:3001 (local dev)
 * - ENVIRONMENT!=local: https://waste-movement-external-api.api.${environment}.cdp-int.defra.cloud
 */
String environment = props.get("environment") ?: "local"

String domain
String protocol
String port
String basePath

if (environment == "local") {
    // Local development: localhost
    domain = "localhost"
    protocol = "http"
    port = "3001"
    basePath = ""
} else {
    // CDP: direct public API service URL
    domain = "waste-movement-external-api.api.${environment}.cdp-int.defra.cloud"
    protocol = "https"
    port = "443"
    basePath = ""
}

props.put("wasteMovementExternalApiDomain", domain)
props.put("wasteMovementExternalApiProtocol", protocol)
props.put("wasteMovementExternalApiPort", port)
props.put("wasteMovementExternalApiBasePath", basePath)

log.info("Waste Movement External API URL: ${protocol}://${domain}:${port}${basePath}")
