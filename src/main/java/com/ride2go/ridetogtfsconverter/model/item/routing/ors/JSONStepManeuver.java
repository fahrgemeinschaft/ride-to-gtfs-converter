package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class JSONStepManeuver {

	private Double[] location;

	@JsonProperty("bearing_before")
	private Integer bearingBefore;

	@JsonProperty("bearing_after")
	private Integer bearingAfter;
}
