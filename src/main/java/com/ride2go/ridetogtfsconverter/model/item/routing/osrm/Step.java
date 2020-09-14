package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Step extends Measures {

	private List<Intersection> intersections;

	// left or right
	@JsonProperty("driving_side")
	private String drivingSide;

	// private String geometry;
	private Geometry geometry;

	private String name;

	private String pronunciation;

	private Float weight;

	private String mode;

	private Maneuver maneuver;

	private String ref;

	private String destinations;

	private List<String> exits;

	@JsonProperty("rotary_name")
	private String rotaryName;

	@JsonProperty("rotary_pronunciation")
	private String rotaryPronunciation;
}
