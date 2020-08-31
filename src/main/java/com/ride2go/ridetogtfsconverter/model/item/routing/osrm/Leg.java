package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Leg {

	private List<Step> steps;

	private Float weight;

	// in meter
	private Float distance;

	private String summary;

	// travel time in seconds
	private Float duration;

	private Annotation annotation;
}
