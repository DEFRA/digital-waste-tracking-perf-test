/**
 * Removes x-cdp-request-id header when ENVIRONMENT is not local.
 * Keeps the header when ENVIRONMENT=local (for local dev).
 */

def hm = sampler.getHeaderManager()
if (hm != null && props.get("environment") != "local") {
    hm.removeHeaderNamed("x-cdp-request-id")
}
