// Debug Logger - Logs request/response details when DEBUG=true
// Add as JSR223 PostProcessor to HTTP samplers

def debug = props.get("debug")?.toString()?.equalsIgnoreCase("true")

if (debug) {
    def status = prev.isSuccessful() ? "SUCCESS" : "FAILED"
    
    log.info("================================================================================")
    log.info("[${status}] ${prev.getSampleLabel()} - ${prev.getResponseCode()} (${prev.getTime()}ms)")
    log.info("URL: ${prev.getUrlAsString()}")
    log.info("--- REQUEST HEADERS ---")
    log.info(prev.getRequestHeaders() ?: "(empty)")
    log.info("--- REQUEST BODY ---")
    log.info(prev.getSamplerData() ?: "(empty)")
    log.info("--- RESPONSE HEADERS ---")
    log.info(prev.getResponseHeaders() ?: "(empty)")
    log.info("--- RESPONSE BODY ---")
    log.info(prev.getResponseDataAsString() ?: "(empty)")
    log.info("================================================================================")
}
