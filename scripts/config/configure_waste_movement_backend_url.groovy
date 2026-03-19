/**
 * Derives waste movement backend URL components from ENVIRONMENT and CI.
 *
 * - CI=true:  https://waste-movement-backend.${environment}.cdp-int.defra.cloud
 * - CI=false, ENVIRONMENT=local: http://localhost:3002 (local dev backend)
 * - CI=false, ENVIRONMENT!=local: https://ephemeral-protected.api.${environment}.cdp-int.defra.cloud/waste-movement-backend
 */
String environment = props.get("environment") ?: "local"
boolean ci = props.get("ci")?.toString()?.equalsIgnoreCase("true") ?: false

String domain
String protocol
String port
String basePath

if (ci) {
    // CDP Platform: direct service URL
    domain = "waste-movement-backend.${environment}.cdp-int.defra.cloud"
    protocol = "https"
    port = "443"
    basePath = ""
} else if (environment == "local") {
    // Local development: localhost backend
    domain = "localhost"
    protocol = "http"
    port = "3002"
    basePath = ""
} else {
    // Local execution against CDP (ephemeral gateway)
    domain = "ephemeral-protected.api.${environment}.cdp-int.defra.cloud"
    protocol = "https"
    port = "443"
    basePath = "/waste-movement-backend"
}

props.put("wasteMovementBackendDomain", domain)
props.put("wasteMovementBackendProtocol", protocol)
props.put("wasteMovementBackendPort", port)
props.put("wasteMovementBackendBasePath", basePath)

log.info("Waste Movement Backend URL: ${protocol}://${domain}:${port}${basePath}")
