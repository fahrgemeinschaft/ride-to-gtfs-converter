package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class GeoJSONSummary {

	private List<JSONSegment> segments;

	private JSONSummary summary;

	@JsonProperty("way_points")
	private List<Integer> wayPoints;

	private Map<String, JSONExtra> extras;

	private List<RouteWarning> warnings;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double ascent;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double descent;
}
