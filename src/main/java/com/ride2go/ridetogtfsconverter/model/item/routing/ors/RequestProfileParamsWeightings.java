package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class RequestProfileParamsWeightings {

	@JsonProperty("steepness_difficulty")
	private Integer steepnessDifficulty;

	private Float green;

	private Float quiet;
}
