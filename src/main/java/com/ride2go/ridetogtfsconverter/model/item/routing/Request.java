package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.List;

import lombok.Data;

@Data
public class Request {

	private Coordinate origin;

	private Coordinate destination;

	private List<Coordinate> intermediateStops;
}
