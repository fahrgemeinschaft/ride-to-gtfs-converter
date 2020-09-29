package com.ride2go.ridetogtfsconverter.model.data.ride;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "fg_trips_routing_places")
public class EntityRoutingPlace {

	@Id
	@Column(name = "placeID")
	private String placeId;

	private String address;

	private Double lat;

	private Double lon;
}
