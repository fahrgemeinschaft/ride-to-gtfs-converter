package com.ride2go.ridetogtfsconverter.model.item;

import lombok.Data;

@Data
public class GeoCoordinates {

	private double latitude;

	private double longitude;

	public GeoCoordinates(GeoCoordinates geoCoordinates) {
		if (geoCoordinates == null) {
			throw new NullPointerException("GeoCoordinates can't be created with null parameter");
		}
		this.latitude = geoCoordinates.getLatitude();
		this.longitude = geoCoordinates.getLongitude();
	}

	public GeoCoordinates(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
