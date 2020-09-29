package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import lombok.Data;

@Data
public class Measures {

	// in meter
	private Float distance;

	// travel time in seconds
	private Float duration;
}
