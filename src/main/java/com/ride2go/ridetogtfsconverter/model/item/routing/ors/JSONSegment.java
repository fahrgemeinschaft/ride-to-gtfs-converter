package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class JSONSegment {

	private Double distance;

	private Double duration;

	private List<JSONStep> steps;

	private Double detourfactor;

	private Double percentage;

	private Double avgspeed;

	private Double ascent;

	private Double descent;
}
