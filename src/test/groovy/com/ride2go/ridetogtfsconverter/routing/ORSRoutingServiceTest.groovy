package com.ride2go.ridetogtfsconverter.routing

import static com.ride2go.ridetogtfsconverter.routing.RoutingUtil.*

class ORSRoutingServiceTest extends RoutingSpecification {

	private RoutingService service = new ORSRoutingService()

	def "A valid routing GET request should work and return the right results"() {
		given:
			request.origin = BERLIN
			request.destination = HAMBURG

		when:
			final response = service.calculateRoute(request)
			print "ORS routing result: ${service.convertToJSON(response)}"

		then:
			resultIs(response)

		when:
			int addressExistsInPercentage = calculateFieldExistence(response.drivingInstructionPoints, 'address')

		then:
			addressExistsInPercentage >= 89
	}
}
