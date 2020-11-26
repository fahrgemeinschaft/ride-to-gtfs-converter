package com.ride2go.ridetogtfsconverter.validation;

import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.FEED_END_DATE;
import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.FEED_START_DATE;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;

@Service
public class Constraints {

	private static final Logger LOG = LoggerFactory.getLogger(Constraints.class);

	private static final List<DayOfWeek> FEED_TIME_PERIOD_WEEK_DAYS = getFeedTimePeriodWeekDays();

	public static final String AREA_BADEN_WUERTTEMBERG = "Baden-Wuerttemberg";

	private static final GeoCoordinates COORD_BADEN_WUERTTEMBERG_LEFT_BOTTOM = new GeoCoordinates(47.4, 7.4);

	private static final GeoCoordinates COORD_BADEN_WUERTTEMBERG_RIGHT_TOP = new GeoCoordinates(49.9, 10.6);

	@Value("${custom.gtfs.trips.use-time-period}")
	private boolean useTimePeriod;

	@Value("${custom.gtfs.trips.area}")
	private String area;

	public void ongoingTrips(List<EntityTrip> trips) {
		EntityTrip trip;
		for (int i = 0; i < trips.size(); i++) {
			trip = trips.get(i);
			if (trip.getStartdate() != null && !trip.getStartdate().isBefore(FEED_START_DATE)) {
				// ongoing
			} else if (trip.getReoccurs() != null && trip.getReoccurs().doesReoccur()) {
				// ongoing
			} else {
				LOG.debug("Remove expired Trip with id: " + trip.getTripId());
				trips.remove(i);
				i--;
			}
		}
		if (useTimePeriod) {
			for (int i = 0; i < trips.size(); i++) {
				trip = trips.get(i);
				if (trip.getStartdate() != null && !trip.getStartdate().isAfter(FEED_END_DATE)) {
					// within period
				} else if (trip.getReoccurs() != null
						&& !Collections.disjoint(FEED_TIME_PERIOD_WEEK_DAYS, trip.getReoccurs().getReoccurDays())) {
					// within period
				} else {
					LOG.debug("Remove Trip after feed time period with id: " + trip.getTripId());
					trips.remove(i);
					i--;
				}
			}
		}
	}

	public void ongoingMissingreoccurs(List<EntityTrip> trips) {
		List<ZonedDateTime> missingreoccurs;
		LocalDate missingreoccursItem;
		for (EntityTrip trip : trips) {
			missingreoccurs = trip.getMissingreoccurs();
			if (missingreoccurs != null) {
				for (int i = 0; i < missingreoccurs.size(); i++) {
					missingreoccursItem = missingreoccurs.get(i).toLocalDate();
					if (missingreoccursItem.isBefore(FEED_START_DATE)
							|| (useTimePeriod && missingreoccursItem.isAfter(FEED_END_DATE))) {
						LOG.debug("Remove missingreoccurs day: " + missingreoccursItem);
						missingreoccurs.remove(i);
						i--;
					}
				}
			}
		}
	}

	public void withinArea(List<Offer> offers) {
		if (area.equals(AREA_BADEN_WUERTTEMBERG)) {
			boolean withinArea;
			double lat, lon;
			for (int i = 0; i < offers.size(); i++) {
				withinArea = false;
				for (Place place : offers.get(i).getPlaces()) {
					lat = place.getGeoCoordinates().getLatitude();
					lon = place.getGeoCoordinates().getLongitude();
					if (Double.compare(lat, COORD_BADEN_WUERTTEMBERG_LEFT_BOTTOM.getLatitude()) > 0
							&& Double.compare(COORD_BADEN_WUERTTEMBERG_RIGHT_TOP.getLatitude(), lat) > 0
							&& Double.compare(lon, COORD_BADEN_WUERTTEMBERG_LEFT_BOTTOM.getLongitude()) > 0
							&& Double.compare(COORD_BADEN_WUERTTEMBERG_RIGHT_TOP.getLongitude(), lon) > 0) {
						withinArea = true;
						break;
					}
				}
				if (!withinArea) {
					offers.remove(i);
					i--;
				}
			}
		}
	}

	private static List<DayOfWeek> getFeedTimePeriodWeekDays() {
		List<DayOfWeek> feedTimePeriodWeekDays = new ArrayList<>();
		DayOfWeek feedStartDay = FEED_START_DATE.getDayOfWeek();
		feedTimePeriodWeekDays.add(feedStartDay);
		int i = 1;
		while (i < 7 && !FEED_START_DATE.plusDays(i).isAfter(FEED_END_DATE)) {
			feedTimePeriodWeekDays.add(feedStartDay.plus(i++));
		}
		return feedTimePeriodWeekDays;
	}
}
