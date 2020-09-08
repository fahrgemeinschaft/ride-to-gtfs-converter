package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.List;

import lombok.Data;

@Data
public class Response {

	private List<Location> instructionPoints;

	private List<Location> intersectionPoints;

	private List<Location> routeShapingPoints;

	private Float duration;
}
