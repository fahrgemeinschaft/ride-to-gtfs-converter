package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class RequestProfileParamsRestrictions {

	private Float length;

	private boolean hasLength;

	private Float width;

	private Float height;

	private Float axleload;

	private Float weight;

	private boolean hazmat;

	@JsonProperty("surface_type")
	private String surfaceType;

	@JsonProperty("track_type")
	private String trackType;

	@JsonProperty("smoothness_type")
	private String smoothnessType;

	@JsonProperty("maximum_sloped_kerb")
	private Float maximumSlopedKerb;

	@JsonProperty("maximum_incline")
	private Integer maximumIncline;

	@JsonProperty("minimum_width")
	private Float minimumWidth;
}
