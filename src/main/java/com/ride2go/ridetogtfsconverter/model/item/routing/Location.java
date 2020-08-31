package com.ride2go.ridetogtfsconverter.model.item.routing;

import lombok.Data;

@Data
public class Location {

	private Coordinate coordinate;

	private String name;
}
