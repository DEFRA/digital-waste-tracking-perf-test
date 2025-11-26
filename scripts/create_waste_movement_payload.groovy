// Get variables from JMeter system properties
String apiCode = props.get("apiCode")
String testType = vars.get("testType")
String timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
String threadNum = ctx.getThreadNum().toString()
String wasteDescription1 = "Create Waste Movement: ${testType} - ${threadNum} - Primary waste containing industrial waste and hazardous heavy metals"
String wasteDescription2 = "Create Waste Movement: ${testType} - ${threadNum} - Secondary waste containing plastic packaging and minor contaminants"

// JSON payload as string with interpolation
String postPayload = """{
  "apiCode": "${apiCode}",
  "dateTimeReceived": "${timestamp}",
  "reasonForNoConsignmentCode": "NO_DOC_WITH_WASTE",
  "wasteItems": [
    {
      "ewcCodes": [
        "200121"
      ],
      "wasteDescription": "${wasteDescription1}",
      "physicalForm": "Mixed",
      "numberOfContainers": 15,
      "typeOfContainers": "SKI",
      "weight": {
        "metric": "Tonnes",
        "amount": 1.2,
        "isEstimate": true
      },
      "containsPops": true,
      "pops": {
        "sourceOfComponents": "PROVIDED_WITH_WASTE",
        "components": [
          {
            "code": "CHL",
            "concentration": 250
          },
          {
            "code": "TOX",
            "concentration": 156.4
          },
          {
            "code": "DCF",
            "concentration": 0.8
          },
          {
            "code": "DDT",
            "concentration": 1.2
          }
        ]
      },
      "containsHazardous": true,
      "hazardous": {
        "hazCodes": [
          "HP_1",
          "HP_3",
          "HP_6"
        ],
        "sourceOfComponents": "PROVIDED_WITH_WASTE",
        "components": [
          {
            "name": "Mercury",
            "concentration": 0.35
          },
          {
            "name": "Arsenic",
            "concentration": 300
          },
          {
            "name": "Chromium",
            "concentration": 0.42
          },
          {
            "name": "Lead",
            "concentration": 0.89
          }
        ]
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
    },
    {
      "ewcCodes": [
        "150110"
      ],
      "wasteDescription": "${wasteDescription2}",
      "physicalForm": "Solid",
      "numberOfContainers": 5,
      "typeOfContainers": "SKI",
      "weight": {
        "metric": "Tonnes",
        "amount": 1.1,
        "isEstimate": true
      },
      "containsPops": false,
      "pops": {
        "sourceOfComponents": "NOT_PROVIDED"
      },
      "containsHazardous": true,
      "hazardous": {
        "hazCodes": [
          "HP_6"
        ],
        "sourceOfComponents": "PROVIDED_WITH_WASTE",
        "components": [
          {
            "name": "Arsenic",
            "concentration": 75
          }
        ]
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
    "registrationNumber": "CBDL999999",
    "address": {
      "fullAddress": "321 Test Street, Test City",
      "postcode": "TC2 2CD"
    },
    "emailAddress": "test@carrier.com",
    "phoneNumber": "01234567890",
    "meansOfTransport": "Road",
    "vehicleRegistration": "AB12 CDE"
  },
  "receiver": {
    "siteName": "Receiver Ltd",
    "emailAddress": "receiver@test.com",
    "phoneNumber": "01234567890",
    "authorisationNumber": "PPC/A/SEPA9999-9999",
    "regulatoryPositionStatements": [
      123,
      456
    ]
  },
  "receipt": {
    "address": {
      "fullAddress": "123 Test Street, Test City",
      "postcode": "TC1 2AB"
    }
  }
}"""

// Set the payload variable
vars.put("postPayload", postPayload)