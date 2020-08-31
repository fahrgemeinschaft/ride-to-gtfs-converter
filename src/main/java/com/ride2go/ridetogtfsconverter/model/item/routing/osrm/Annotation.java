package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Annotation {

	// in meter
	private List<Float> distance;

	// travel times in seconds
	private List<Float> duration;

	// values are 0 or 1
	private List<Integer> datasources;

	// OSM node IDs
	private List<Long> nodes;

	private List<Long> weight;

	private List<Float> speed;

	private Metadata metadata;
}
