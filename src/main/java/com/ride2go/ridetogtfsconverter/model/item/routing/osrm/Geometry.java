package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Geometry {

	private String type;

	private List<List<Double>> coordinates;
}
