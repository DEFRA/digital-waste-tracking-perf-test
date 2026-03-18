if (prev.getResponseCode().startsWith("4") || prev.getResponseCode().startsWith("5")) {
    logAndPrint("================================================================================")
    logAndPrint("[${prev.getResponseCode()}] ${prev.getSampleLabel()} - ${prev.getResponseCode()} (${prev.getTime()}ms)")
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