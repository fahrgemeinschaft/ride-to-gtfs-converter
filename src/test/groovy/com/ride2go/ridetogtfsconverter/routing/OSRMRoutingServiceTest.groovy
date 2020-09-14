package com.ride2go.ridetogtfsconverter.routing

import static com.ride2go.ridetogtfsconverter.routing.RoutingUtil.*

class OSRMRoutingServiceTest extends RoutingSpecification {

	private RoutingService service = new OSRMRoutingService()

	def "A valid routing GET request should work and return the right results"() {
		given:
			request.origin = BERLIN
			request.destination = HAMBURG

		when:
			final response = service.calculateRoute(request)
			print "OSRM routing result: ${service.convertToJSON(response)}"

		then:
			resultIs(response)
			with (response) {
				streetIntersectionPoints != null
				streetIntersectionPoints.size() > 200
				streetIntersectionPoints.each {
					assert it.geoCoordinates != null
				}
				routeShapingPoints.each {
					assert it.osmNodeId != null
					assert it.distance != null
					assert it.duration != null
				}
			}

		when:
			int addressExistsInPercentage = calculateFieldExistence(response.drivingInstructionPoints, 'address')

		then:
			addressExistsInPercentage >= 81
	}
}
