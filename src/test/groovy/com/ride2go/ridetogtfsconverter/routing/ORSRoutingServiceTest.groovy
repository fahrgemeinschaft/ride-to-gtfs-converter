package com.ride2go.ridetogtfsconverter.routing

import static com.ride2go.ridetogtfsconverter.routing.RoutingUtil.*
import com.fasterxml.jackson.databind.ObjectMapper

class ORSRoutingServiceTest extends RoutingSpecification {

	private RoutingService service = new ORSRoutingService()

	def setup() {
		initService(service)
		jsonConverter.objectMapper = new ObjectMapper()
	}

	def "A valid routing GET request should work and return the right results"() {
		given:
			request.origin = BERLIN
			request.destination = HAMBURG

		when:
			final response = service.calculateRoute(request)
			println "ORS routing result: ${jsonConverter.toJSONString(response)}"

		then:
			resultIs(response)

		when:
			int addressExistsInPercentage = calculateFieldExistence(response.drivingInstructionPoints, 'address')

		then:
			addressExistsInPercentage >= 89
	}
}
