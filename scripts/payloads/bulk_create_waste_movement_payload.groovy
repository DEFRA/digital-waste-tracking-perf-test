import groovy.json.JsonBuilder

String testType = vars.get("testType")
String timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
String threadNum = ctx.getThreadNum().toString()

String orgId = UUID.randomUUID().toString()

int movementCount = Integer.parseInt(vars.get("bulkMovementCount") ?: "500")
def movements = (1..movementCount).collect { idx ->
  [
    submittingOrganisation: [
      defraCustomerOrganisationId: orgId
    ],
    dateTimeReceived: timestamp,
    reasonForNoConsignmentCode: "NO_DOC_WITH_WASTE",
    wasteItems: [
      [
        ewcCodes: ["150110"],
        wasteDescription: "Bulk Create: ${testType} - ${threadNum} - Movement ${idx} - Secondary waste containing plastic packaging and minor contaminants",
        physicalForm: "Solid",
        numberOfContainers: 5,
        typeOfContainers: "SKI",
        weight: [metric: "Tonnes", amount: 1.1, isEstimate: true],
        containsPops: false,
        pops: [sourceOfComponents: "NOT_PROVIDED"],
        containsHazardous: true,
        hazardous: [
          hazCodes: ["HP_6"],
          sourceOfComponents: "PROVIDED_WITH_WASTE",
          components: [
            [name: "Arsenic", concentration: 75]
          ]
        ],
        disposalOrRecoveryCodes: [
          [code: "R1", weight: [metric: "Tonnes", amount: 0.75, isEstimate: false]]
        ]
      ]
    ],
    carrier: [
      organisationName: "Carrier Ltd",
      registrationNumber: "CBDL999999",
      address: [fullAddress: "321 Test Street, Test City", postcode: "TC2 2CD"],
      emailAddress: "test@carrier.com",
      phoneNumber: "01234567890",
      meansOfTransport: "Road",
      vehicleRegistration: "AB12 CDE"
    ],
    receiver: [
      siteName: "Receiver Ltd",
      emailAddress: "receiver@test.com",
      phoneNumber: "01234567890",
      authorisationNumber: "PPC/A/SEPA9999-9999",
      regulatoryPositionStatements: [123, 456]
    ],
    receipt: [
      address: [fullAddress: "123 Test Street, Test City", postcode: "TC1 2AB"]
    ]
  ]
}

String payload = new JsonBuilder(movements).toString()
vars.put("bulkPostPayload", payload)
