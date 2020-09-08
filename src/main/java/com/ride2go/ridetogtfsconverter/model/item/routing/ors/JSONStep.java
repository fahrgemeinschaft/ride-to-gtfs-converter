package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class JSONStep {

	private Double distance;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double duration;

	private Integer type;

	private String instruction;

	private String name;

	@JsonProperty("exit_number")
	private Integer exitNumber;

	@JsonProperty("exit_bearings")
	private Integer[] exitBearings;

	@JsonProperty("way_points")
	private Integer[] wayPoints;

	private JSONStepManeuver maneuver;
}
