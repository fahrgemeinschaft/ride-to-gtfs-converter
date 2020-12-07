package com.ride2go.ridetogtfsconverter.gtfs;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class OBAWriterParameter {

	private OBAWriterParameter() {
	}

	protected static final int MISCELLANEOUS_SERVICE = 1700;

	protected static final String ONE_DIRECTION = "0";

	protected static final int EXACT_TIMEPOINT = 1;

	protected static final int APPROXIMATE_TIMEPOINT = 0;

	protected static final int SERVICE_AVAILABLE = 1;

	protected static final int SERVICE_NOT_AVAILABLE = 2;

	public static LocalDate feedStartDate;

	public static LocalDate feedEndDate;

	protected static ServiceDate obaFeedStartDate;

	protected static ServiceDate obaFeedEndDate;

	protected static ServiceDate getByDate(LocalDate date) {
		return new ServiceDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}

	protected static ServiceDate getByDateTime(ZonedDateTime dateTime) {
		return new ServiceDate(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth());
	}
}
