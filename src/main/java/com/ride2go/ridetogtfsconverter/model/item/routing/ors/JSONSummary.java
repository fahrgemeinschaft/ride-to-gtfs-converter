package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class JSONSummary {

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.2d")
	private Double distance;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double duration;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double ascent;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "%.1d")
	private Double descent;
}
