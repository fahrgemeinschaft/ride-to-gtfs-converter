package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Waypoint {

	private String hint;

	// [longitude, latitude]
	private List<Double> location;

	private String name;

	// in meter
	private Float distance;
}
