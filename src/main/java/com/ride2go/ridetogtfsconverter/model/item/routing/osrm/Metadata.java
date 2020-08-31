package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Metadata {

	@JsonProperty("datasource_names")
	private List<String> datasourceNames;
}
