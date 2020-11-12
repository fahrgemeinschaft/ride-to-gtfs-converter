package com.ride2go.ridetogtfsconverter.model.data.ride;

import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import com.ride2go.ridetogtfsconverter.conversion.LocalTimeAttributeConverter;

import lombok.Data;

@Data
@Entity
@Table(name = "fg_trips_routing_places")
public class EntityRoutingPlace {

	@Id
	@Column(name = "placeID")
	@OrderColumn(name = "ix_trips_routing_places_placeID")
	private String placeId;

	private String address;

	private Double lat;

	private Double lon;

	@Column(columnDefinition = "INT(11)")
	@Convert(converter = LocalTimeAttributeConverter.class)
	private LocalTime stoptime;
}
