package com.ride2go.ridetogtfsconverter.routing

import com.ride2go.ridetogtfsconverter.conversion.JSONConverter
import com.ride2go.ridetogtfsconverter.model.item.routing.Request
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import spock.lang.Specification

@RunWith(SpringRunner.class)
@SpringBootTest
class RoutingSpecification extends Specification {

	Request request = new Request()
	
	@Autowired
	JSONConverter jsonConverter

	void resultIs(response) {
		assert response != null
		with (response) {
			assert drivingInstructionPoints != null
			assert drivingInstructionPoints.size() > 20
			assert drivingInstructionPoints.each {
				assert it.geoCoordinates != null
				assert it.distance != null
				assert it.duration != null
			}
			assert routeShapingPoints != null
			assert routeShapingPoints.size() > 1000
			assert routeShapingPoints.each {
				assert it.geoCoordinates != null
			}
			assert distance != null
			assert duration != null
		}
	}

	def calculateFieldExistence(list, field) {
		int count = 0
		list.each {
			if (it[field] != null) {
				count++
			}
		}
		100 * count / list.size()
	}
}
