import java.util.concurrent.atomic.AtomicLong

/**
 * Registers a shutdown hook once, seeds cumulative total from temp state file (multi-JVM runs),
 * and writes results/bulk-movement-metrics.txt plus temp/bulk-upload-cumulative-state.txt on exit.
 */
if (props.get("bulkUploadMetricsInitialized") == null) {
    props.put("bulkUploadMetricsInitialized", "true")
    props.put("bulkUploadMetricsStartMs", Long.toString(System.currentTimeMillis()))

    def userDir = System.getProperty("user.dir")
    def stateFile = new File(userDir + "/temp/bulk-upload-cumulative-state.txt")

    long persistedSeed = 0L
    if (stateFile.exists()) {
        try {
            def txt = stateFile.getText("UTF-8")?.trim()
            if (txt) {
                persistedSeed = Long.parseLong(txt)
            }
        } catch (Throwable t) {
            log.warn("bulk_upload_metrics_init: could not read persistent cumulative state: " + t.getMessage())
        }
    }
    props.put("bulkUploadCumulativeInitialSeed", Long.toString(persistedSeed))
    props.put("bulkUploadTotalMovements", new AtomicLong(persistedSeed))

    Runtime.runtime.addShutdownHook(new Thread({
        try {
            def raw = props.get("bulkUploadTotalMovements")
            long total = 0L
            if (raw instanceof AtomicLong) {
                total = ((AtomicLong) raw).get()
            } else if (raw != null) {
                total = Long.parseLong(raw.toString())
            }
            long startMs = Long.parseLong(props.get("bulkUploadMetricsStartMs")?.toString() ?: "0")
            long endMs = System.currentTimeMillis()
            double durSec = Math.max((endMs - startMs) / 1000.0d, 1e-9d)
            long seed = Long.parseLong(props.get("bulkUploadCumulativeInitialSeed")?.toString() ?: "0")
            long thisJvm = total - seed
            double rateThisJvm = thisJvm / durSec

            def userDirHook = System.getProperty("user.dir")
            def resultsFile = new File(userDirHook + "/results/bulk-movement-metrics.txt")
            resultsFile.parentFile?.mkdirs()
            def summary = """Bulk upload endpoint — movement counts (this JVM process)
Total bulk movements (cumulative, includes prior JMX runs if any): ${total}
Movements from prior JMeter processes (seeded from temp state): ${seed}
Movements this JMeter process only: ${thisJvm}
Wall-clock duration this process (approx., s): ${String.format("%.3f", durSec)}
Average bulk movements per second (this process only): ${String.format("%.2f", rateThisJvm)}
"""
            resultsFile.setText(summary, "UTF-8")
            log.info("BULK_UPLOAD_METRICS " + summary.replace("\n", " | ").trim())

            stateFile.parentFile?.mkdirs()
            stateFile.setText(Long.toString(total), "UTF-8")
        } catch (Throwable t) {
            log.warn("bulk_upload_metrics_init shutdown hook failed: " + t.getMessage(), t)
        }
    } as Runnable))
}
