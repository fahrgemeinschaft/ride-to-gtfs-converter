package com.ride2go.ridetogtfsconverter.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateAndTimeHandler {

	private DateAndTimeHandler() {
	}

	public static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("Hmm");

	public static final LocalDate TODAY = LocalDate.now(ZoneId.of("Europe/Berlin"));

	public static final LocalDate YESTERDAY = TODAY.minusDays(1);

	public static final LocalDate ONE_MONTH_FROM_TODAY = TODAY.plusMonths(1);
}
