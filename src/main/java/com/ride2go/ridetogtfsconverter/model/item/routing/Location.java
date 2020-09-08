package com.ride2go.ridetogtfsconverter.model.item.routing;

import lombok.Data;

@Data
public class Location {

	private Coordinates coordinate;

	private String address;

	private Float duration;
}
