package com.ride2go.ridetogtfsconverter.model.item.routing.osrm;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Leg extends Measures {

	private List<Step> steps;

	private Float weight;

	private String summary;

	private Annotation annotation;
}
