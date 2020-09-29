package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.List;

import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;

import lombok.Data;

@Data
public class Request {

	private GeoCoordinates origin;

	private GeoCoordinates destination;

	private List<GeoCoordinates> intermediateStops;
}
