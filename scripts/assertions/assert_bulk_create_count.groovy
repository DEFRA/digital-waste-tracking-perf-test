def json = new groovy.json.JsonSlurper().parseText(prev.getResponseDataAsString())
int expected = Integer.parseInt(vars.get("bulkMovementCount") ?: "250")
int actual = json.movements.size()

if (actual != expected) {
    AssertionResult.setFailure(true)
    AssertionResult.setFailureMessage("Expected ${expected} waste tracking IDs but got ${actual}")
}
