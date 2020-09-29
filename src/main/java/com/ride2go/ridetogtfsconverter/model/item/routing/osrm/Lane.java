package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;

@Data
public class Lane {

	private Boolean valid;

	private List<String> indications;
}
