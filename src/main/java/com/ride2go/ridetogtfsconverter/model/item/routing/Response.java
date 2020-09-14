package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Response {

	private List<Location> drivingInstructionPoints = new ArrayList<>();

	private List<Location> streetIntersectionPoints = new ArrayList<>();

	private List<Location> routeShapingPoints = new ArrayList<>();

	private Double duration;

	private Double distance;

	public void addDrivingInstructionPoint(Location drivingInstructionPoint) {
		this.drivingInstructionPoints.add(drivingInstructionPoint);
	}

	public void addStreetIntersectionPoint(Location streetIntersectionPoint) {
		this.streetIntersectionPoints.add(streetIntersectionPoint);
	}

	public void addRouteShapingPoint(Location routeShapingPoint) {
		this.routeShapingPoints.add(routeShapingPoint);
	}

	public void addDrivingInstructionPoints(List<Location> drivingInstructionPoints) {
		this.drivingInstructionPoints.addAll(drivingInstructionPoints);
	}

	public void addStreetIntersectionPoints(List<Location> streetIntersectionPoints) {
		this.streetIntersectionPoints.addAll(streetIntersectionPoints);
	}

	public void addRouteShapingPoints(List<Location> routeShapingPoints) {
		this.routeShapingPoints.addAll(routeShapingPoints);
	}
}
