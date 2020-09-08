package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class RouteRequestOptions {

	@JsonProperty("avoid_features")
	private String[] avoidFeatures;

	@JsonProperty("avoid_borders")
	private String avoidBorders;

	@JsonProperty("avoid_countries")
	private String[] avoidCountries;

	@JsonProperty("vehicle_type")
	private String vehicleType;

	@JsonProperty("profile_params")
	private RequestProfileParams profileParams;

	@JsonProperty("avoid_polygons")
	private JSONObject avoidPolygons;

	@JsonProperty("round_trip")
	private RouteRequestRoundTripOptions roundTrip;
}
