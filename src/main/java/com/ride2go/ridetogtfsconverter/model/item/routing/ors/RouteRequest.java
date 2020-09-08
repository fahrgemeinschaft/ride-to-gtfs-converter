package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class RouteRequest {

	private String id;

	private List<List<Double>> coordinates;

	private String profile;

	private String preference;

	private String format;

	private String units;

	private String language;

	private boolean geometry;

	private boolean instructions;

	@JsonProperty("instructions_format")
	private String instructionsFormat;

	@JsonProperty("roundabout_exits")
	private boolean roundaboutExits;

	private String[] attributes;

	private boolean maneuvers;

	private Double[] radiuses;

	private Double[][] bearings;

	@JsonProperty(value = "continue_straight")
	private boolean continueStraight;

	private boolean elevation;

	@JsonProperty("extra_info")
	private String[] extraInfo;

	private boolean optimized;

	private RouteRequestOptions options;

	@JsonProperty("suppress_warnings")
	private boolean suppressWarnings;

	@JsonProperty("geometry_simplify")
	private boolean geometrySimplify;

	@JsonProperty("skip_segments")
	private List<Integer> skipSegments;

	@JsonProperty("alternative_routes")
	private RouteRequestAlternativeRoutes alternativeRoutes;

	@JsonProperty("maximum_speed")
	private double maximumSpeed;
}
