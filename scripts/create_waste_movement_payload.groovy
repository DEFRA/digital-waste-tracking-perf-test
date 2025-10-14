// Get variables from JMeter system properties
String organisationApiId = props.get("organisationApiId")
String testType = vars.get("testType") ?: "Baseline"
String timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
String threadNum = ctx.getThreadNum().toString()

// JSON payload as string with interpolation
String payload = """{
  "organisationApiId": "${organisationApiId}",
  "dateTimeReceived": "${timestamp}",
  "wasteItems": [
    {
      "ewcCodes": [
        "020101"
      ],
      "wasteDescription": "Peformance test: ${testType} - ${threadNum}",
      "physicalForm": "Mixed",
      "numberOfContainers": 3,
      "typeOfContainers": "SKI",
      "weight": {
        "metric": "Tonnes",
        "amount": 2.5,
        "isEstimate": false
      },
      "disposalOrRecoveryCodes": [
        {
          "code": "R1",
          "weight": {
            "metric": "Tonnes",
            "amount": 0.75,
            "isEstimate": false
          }
        }
      ]
    }
  ],
  "carrier": {
    "organisationName": "Carrier Ltd",
    "registrationNumber": "REG123456",
    "meansOfTransport": "Rail"
  },
  "receiver": {
    "organisationName": "Receiver Ltd",
    "emailAddress": "receiver@test.com",
    "authorisationNumbers": ["PPC/A/9999999"]
  },
  "receipt": {
    "address": {
      "fullAddress": "123 Test Street, Test City",
      "postcode": "TC1 2AB"
    }
  }
}"""

// Set the payload variable
vars.put("payload", payload)