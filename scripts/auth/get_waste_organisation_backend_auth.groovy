import java.util.Base64

// Check if Waste Organisation Backend Basic auth header already exists in global properties
if (props.get("base64EncodedCredentialWasteOrganisationBackend") == null) {
    log.info("No Waste Organisation Backend auth header found, generating...")

    String serviceAuthUsernameWasteOrganisationBackend = "waste-organisation-backend"
    String serviceAuthPasswordWasteOrganisationBackend = props.get("serviceAuthPasswordWasteOrganisationBackend")

    if (!serviceAuthPasswordWasteOrganisationBackend) {
        String errorMsg = "Missing required property: serviceAuthPasswordWasteOrganisationBackend (set SERVICE_AUTH_PASSWORD_WASTE_ORGANISATION_BACKEND)"
        log.error(errorMsg)
        throw new Exception(errorMsg)
    }

    String credentials = "${serviceAuthUsernameWasteOrganisationBackend}:${serviceAuthPasswordWasteOrganisationBackend}"
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"))

    props.put("base64EncodedCredentialWasteOrganisationBackend", encoded)
    log.info("Successfully generated and stored global Waste Organisation Backend Basic auth header")
} else {
    log.info("Reusing existing global Waste Organisation Backend auth header")
}
