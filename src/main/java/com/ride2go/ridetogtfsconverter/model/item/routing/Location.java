package com.ride2go.ridetogtfsconverter.model.item.routing;

import lombok.Data;

@Data
public class Location {

	private GeoCoordinates geoCoordinates;

	private Long osmNodeId;

	private String address;

	private Double duration;

	private Double distance;

	public Location(GeoCoordinates geoCoordinates) {
		this.geoCoordinates = geoCoordinates;
	}

	public Location(double latitude, double longitude) {
		this.geoCoordinates = new GeoCoordinates(latitude, longitude);
	}
}
