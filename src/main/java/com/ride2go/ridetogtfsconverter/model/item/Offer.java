package com.ride2go.ridetogtfsconverter.model.item;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class Offer {

	private String id;

	private LocalDate startDate;

	private List<Place> places;

	private Recurring recurring;

	private List<ZonedDateTime> missingreoccurs;

	@JsonIgnore
	public Place getOrigin() {
		return places.get(0);
	}

	@JsonIgnore
	public Place getDestination() {
		return places.get(places.size() - 1);
	}
}
