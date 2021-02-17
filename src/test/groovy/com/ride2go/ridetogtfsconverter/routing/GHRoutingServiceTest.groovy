package com.ride2go.ridetogtfsconverter.routing

import static com.ride2go.ridetogtfsconverter.routing.RoutingUtil.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class GHRoutingServiceTest extends RoutingSpecification {

	@Autowired
	@Qualifier("GH")
	private RoutingService service

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
