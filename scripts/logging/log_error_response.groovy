// Debug Logger - Logs request/response details when DEBUG=true
// Add as JSR223 PostProcessor to HTTP samplers

def debug = props.get("debug")?.toString()?.equalsIgnoreCase("true")

if (debug) {
    def status = prev.isSuccessful() ? "SUCCESS" : "FAILED"

    // These go to jmeter.log
    logAndPrint("================================================================================")
    logAndPrint("[${status}] ${prev.getSampleLabel()} - ${prev.getResponseCode()} (${prev.getTime()}ms)")
    logAndPrint("URL: ${prev.getUrlAsString()}")
    logAndPrint("--- REQUEST HEADERS ---")
    logAndPrint(prev.getRequestHeaders() ?: "(empty)")
    logAndPrint("--- REQUEST BODY ---")
    logAndPrint(prev.getSamplerData() ?: "(empty)")
    logAndPrint("--- RESPONSE HEADERS ---")
    logAndPrint(prev.getResponseHeaders() ?: "(empty)")
    logAndPrint("--- RESPONSE BODY ---")
    logAndPrint(prev.getResponseDataAsString() ?: "(empty)")
    logAndPrint("================================================================================")
}

def logAndPrint(logLine) {
    log.info(logLine)
    println(logLine)
}