package com.ride2go.ridetogtfsconverter.model.item.routing;

import lombok.Data;

@Data
public class Location {

	private Coordinates coordinates;

	private String address;

	private Float duration;

	private Float distance;

	public Location(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	public Location(double latitude, double longitude) {
		this.coordinates = new Coordinates(latitude,
				longitude);
	}
}
