package com.ride2go.ridetogtfsconverter.routing

import static com.ride2go.ridetogtfsconverter.routing.RoutingUtil.*
import com.fasterxml.jackson.databind.ObjectMapper

class GHRoutingServiceTest extends RoutingSpecification {

	private RoutingService service = new GHRoutingService()

	def setup() {
		jsonConverter.objectMapper = new ObjectMapper()
	}

	def "A valid routing GET request should work and return the right results"() {
		given:
			request.origin = BERLIN
			request.destination = HAMBURG

		when:
			final response = service.calculateRoute(request)
			println "GH routing result: ${jsonConverter.toJSONString(response)}"

		then:
			resultIs(response)

		when:
			int addressExistsInPercentage = calculateFieldExistence(response.drivingInstructionPoints, 'address')

		then:
			addressExistsInPercentage >= 73
	}
}
