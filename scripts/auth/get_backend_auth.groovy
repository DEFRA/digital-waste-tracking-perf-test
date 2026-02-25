import java.util.Base64

// Check if backend auth header already exists in global properties
if (props.get("global_backend_auth_header") == null) {
    log.info("No backend auth header found, generating...")

    String backendAuthUser = props.get("backendAuthUser") ?: "waste-organisation-backend"
    String backendAuthPassword = props.get("backendAuthPassword")

    if (!backendAuthPassword) {
        String errorMsg = "Missing required property: backendAuthPassword"
        log.error(errorMsg)
        throw new Exception(errorMsg)
    }

    String credentials = "${backendAuthUser}:${backendAuthPassword}"
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"))

    props.put("global_backend_auth_header", encoded)
    log.info("Successfully generated and stored global backend auth header")
} else {
    log.info("Reusing existing global backend auth header")
}