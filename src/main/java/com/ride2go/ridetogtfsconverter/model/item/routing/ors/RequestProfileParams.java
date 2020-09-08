package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RequestProfileParams {

	private RequestProfileParamsWeightings weightings;

	private RequestProfileParamsRestrictions restrictions;
}
