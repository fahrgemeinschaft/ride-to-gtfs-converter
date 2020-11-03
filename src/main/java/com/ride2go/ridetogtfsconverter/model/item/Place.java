package com.ride2go.ridetogtfsconverter.model.item;

import lombok.Data;

@Data
public class Place {

	private String id;

	private GeoCoordinates geoCoordinates;

	private String address;

	private Integer timeInSeconds;
}
