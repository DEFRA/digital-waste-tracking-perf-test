import java.util.concurrent.atomic.AtomicLong
import java.util.Locale

/**
 * Records how many waste movements were sent in this HTTP sample (bulk array size),
 * plus running total across the whole test (all threads).
 * Exposes sample_variables for JTL and CustomGraphConsumer charts.
 */
int count = Integer.parseInt(vars.get("bulkMovementCount") ?: "0")
vars.put("bulkMovementsInRequest", Integer.toString(count))

long cumulativeTotal = 0L
def raw = props.get("bulkUploadTotalMovements")
if (raw instanceof AtomicLong) {
    cumulativeTotal = ((AtomicLong) raw).addAndGet((long) count)
} else {
    cumulativeTotal = (long) count
}
vars.put("bulkMovementsCumulativeTotal", Long.toString(cumulativeTotal))

long elapsedMs = prev.getTime()
double perSec = elapsedMs > 0L ? (count * 1000.0d / (double) elapsedMs) : 0.0d
vars.put("bulkMovementsPerSecond", String.format(Locale.US, "%.4f", perSec))
