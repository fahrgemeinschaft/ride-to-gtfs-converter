package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EngineInfo {

	private String version;

	@JsonProperty("build_date")
	private String buildDate;

	@JsonProperty("graph_date")
	private String graphDate;
}
