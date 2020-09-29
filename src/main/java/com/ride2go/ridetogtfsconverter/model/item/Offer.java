package com.ride2go.ridetogtfsconverter.model.item;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class Offer {

	private String id;

	private LocalDate startDate;

	private LocalTime startTime;

	private Place origin;

	private Place destination;

	private List<Place> intermediatePlaces;

	private Recurring recurring;
}
