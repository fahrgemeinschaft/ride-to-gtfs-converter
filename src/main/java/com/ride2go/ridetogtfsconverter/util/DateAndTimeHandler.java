package com.ride2go.ridetogtfsconverter.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateAndTimeHandler {

	private DateAndTimeHandler() {
	}

	public static final String TIME_ZONE_BERLIN = "Europe/Berlin";

	public static final ZoneId TIME_ZONE_ID_BERLIN = ZoneId.of(TIME_ZONE_BERLIN);

	public static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("Hmm");

	public static final int ONE_DAY_IN_SECONDS = 86400;

	public static LocalDate today;

	public static LocalDate oneMonthFromToday;

	public static LocalDate oneYearFromToday;
}
