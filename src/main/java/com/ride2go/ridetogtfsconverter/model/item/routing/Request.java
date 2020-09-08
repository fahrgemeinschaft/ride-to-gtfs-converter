package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.List;

import lombok.Data;

@Data
public class Request {

	private Coordinates origin;

	private Coordinates destination;

	private List<Coordinates> intermediateStops;
}
