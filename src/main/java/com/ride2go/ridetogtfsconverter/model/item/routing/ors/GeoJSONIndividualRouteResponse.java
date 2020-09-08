package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import org.json.simple.JSONObject;

import lombok.Data;

@Data
public class GeoJSONIndividualRouteResponse {

	private double[] bbox;

	private String type;

	private GeoJSONSummary properties;

	private JSONObject geometry;
}
