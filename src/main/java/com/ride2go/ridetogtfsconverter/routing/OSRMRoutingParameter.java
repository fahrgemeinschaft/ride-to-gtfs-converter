package com.ride2go.ridetogtfsconverter.routing;

public class OSRMRoutingParameter {

	private OSRMRoutingParameter() {
	}

	protected static final String BASE_URI = "http://router.project-osrm.org/route/v1/driving/";

	protected static final boolean STEPS_VALUE = true;

	private enum Overview {
		SIMPLIFIED("simplified"),
		FULL("full"),
		FALSE("false");

		private String value;

		Overview(String value) {
			this.value = value;
		}
	}

	protected static final String OVERVIEW_VALUE = Overview.FULL.value;

	private enum Annotations {
		TRUE("true"),
		FALSE("false"),
		NODES("nodes"),
		DISTANCE("distance"),
		DURATION("duration"),
		DATASOURCES("datasources"),
		WEIGHT("weight"),
		SPEED("speed");

		private String value;

		Annotations(String value) {
			this.value = value;
		}
	}

	protected static final String ANNOTATIONS_VALUE = Annotations.TRUE.value;

	private enum Geometries {
		polyline,
		polyline6,
		geojson
	}

	protected static final Geometries GEOMETRIES_VALUE = Geometries.geojson;
}
