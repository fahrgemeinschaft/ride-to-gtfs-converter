package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Maneuver {

	// angle in degree
	@JsonProperty("bearing_after")
	private Integer bearingAfter;

	// angle in degree
	@JsonProperty("bearing_before")
	private Integer bearingBefore;

	private String type;

	// [longitude, latitude]
	private List<Double> location;

	private String modifier;

	private Integer exit;
}
