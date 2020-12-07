package com.ride2go.ridetogtfsconverter.gtfs;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.ONE_MONTH_FROM_TODAY;
import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TODAY;

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

	public static final LocalDate FEED_START_DATE = TODAY;

	public static final LocalDate FEED_END_DATE = ONE_MONTH_FROM_TODAY;

	protected static final ServiceDate OBA_FEED_START_DATE = getByDate(FEED_START_DATE);

	protected static final ServiceDate OBA_FEED_END_DATE = getByDate(FEED_END_DATE);

	protected static ServiceDate getByDate(LocalDate date) {
		return new ServiceDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}

	protected static ServiceDate getByDateTime(ZonedDateTime dateTime) {
		return new ServiceDate(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth());
	}
}
