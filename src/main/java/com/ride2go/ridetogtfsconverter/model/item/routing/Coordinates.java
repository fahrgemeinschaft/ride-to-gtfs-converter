package com.ride2go.ridetogtfsconverter.model.item.routing;

import lombok.Data;

@Data
public class Coordinates {

	private double latitude;

	private double longitude;

	public Coordinates(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
