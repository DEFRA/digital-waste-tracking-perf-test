def json = new groovy.json.JsonSlurper().parseText(prev.getResponseDataAsString())
vars.put("wasteTrackingIds", json.movements.collect { it.wasteTrackingId }.join(","))
