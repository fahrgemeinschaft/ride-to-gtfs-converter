package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Intersection {

	private Integer out;

	private Integer in;

	private List<Lane> lanes;

	private List<Boolean> entry;

	// [longitude, latitude]
	private List<Double> location;

	// angles in degree
	private List<Integer> bearings;

	private List<String> classes;
}
