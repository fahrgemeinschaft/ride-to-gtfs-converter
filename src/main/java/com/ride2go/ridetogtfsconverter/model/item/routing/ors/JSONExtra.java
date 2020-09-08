package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import java.util.List;

import lombok.Data;

@Data
public class JSONExtra {

	private List<List<Long>> values;

	private List<JSONExtraSummary> summary;
}
