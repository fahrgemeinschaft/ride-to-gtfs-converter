package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Route extends Measures {

	private List<Leg> legs;

	@JsonProperty("weight_name")
	private String weightName;

	// private String geometry;
	private Geometry geometry;

	private Float weight;
}
