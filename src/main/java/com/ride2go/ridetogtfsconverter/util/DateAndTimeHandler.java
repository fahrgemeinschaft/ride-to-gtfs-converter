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

	public static final LocalDate TODAY = LocalDate.now(TIME_ZONE_ID_BERLIN);

	public static final LocalDate ONE_MONTH_FROM_TODAY = TODAY.plusMonths(1);

	public static final LocalDate ONE_YEAR_FROM_TODAY = TODAY.plusYears(1);

	public static final int ONE_DAY_IN_SECONDS = 86400;
}
