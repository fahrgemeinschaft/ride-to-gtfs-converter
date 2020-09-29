package com.ride2go.ridetogtfsconverter.routing

import spock.lang.Specification

class OSMNodeServiceTest extends Specification {

	def "A valid node GET request should work and return the right results"() {
		given:
			final osmId = 1989098258L

		when:
			final geoCoordinates = OSMNodeService.convertIdToLatLon(osmId)

		then:
			geoCoordinates.latitude  == 24.3655948
			geoCoordinates.longitude == 88.6279164
	}
}
