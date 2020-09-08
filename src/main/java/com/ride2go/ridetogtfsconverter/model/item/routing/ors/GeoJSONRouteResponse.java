package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import java.util.List;

import lombok.Data;

@Data
public class GeoJSONRouteResponse {

	private String type;

	private List<GeoJSONIndividualRouteResponse> features;

	private double[] bbox;

	private RouteResponseInfo metadata;
}
