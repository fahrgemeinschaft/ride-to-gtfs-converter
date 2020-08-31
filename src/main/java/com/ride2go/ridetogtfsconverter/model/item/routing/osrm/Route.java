package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Route {

	private List<Leg> legs;

	@JsonProperty("weight_name")
	private String weightName;

	private Geometry geometry;

	private Float weight;

	// in meter
	private Float distance;

	// travel time in seconds
	private Float duration;
}
