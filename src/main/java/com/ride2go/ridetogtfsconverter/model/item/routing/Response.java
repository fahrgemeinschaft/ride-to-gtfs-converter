package com.ride2go.ridetogtfsconverter.model.item.routing;

import java.util.List;

import lombok.Data;

@Data
public class Response {

	private List<Location> generatedStops;

	private Float duration;
}
