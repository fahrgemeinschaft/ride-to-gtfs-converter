package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Response {

	private List<Location> instructionPoints = new ArrayList<>();

	private List<Location> intersectionPoints = new ArrayList<>();

	private List<Location> routeShapingPoints = new ArrayList<>();

	private Float duration;

	private Float distance;

	public void addInstructionPoint(Location instructionPoint) {
		instructionPoints.add(instructionPoint);
	}

	public void addIntersectionPoint(Location intersectionPoint) {
		intersectionPoints.add(intersectionPoint);
	}

	public void addRouteShapingPoint(Location routeShapingPoint) {
		routeShapingPoints.add(routeShapingPoint);
	}
}
