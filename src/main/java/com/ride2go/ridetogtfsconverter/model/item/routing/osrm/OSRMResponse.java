package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class OSRMResponse {

	private String code;

	private List<Waypoint> waypoints;

	private List<Route> routes;
}
