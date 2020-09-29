package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RouteRequestAlternativeRoutes {

	@JsonProperty("target_count")
	private Integer targetCount;

	@JsonProperty("weight_factor")
	private Double weightFactor;

	@JsonProperty("share_factor")
	private Double shareFactor;
}
