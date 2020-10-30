package com.ride2go.ridetogtfsconverter.gtfs;

import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.APPROXIMATE_TIMEPOINT;
import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.EXACT_TIMEPOINT;
import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.MISCELLANEOUS_SERVICE;
import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.ONE_DIRECTION;
import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.ONE_MONTH_FROM_TODAY;
import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TODAY;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.csv_entities.exceptions.MissingRequiredEntityException;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.model.item.Recurring;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;
import com.ride2go.ridetogtfsconverter.routing.RoutingService;

@Service
public class OBAWriterService implements WriterService {

	private static final Logger LOG = LoggerFactory.getLogger(OBAWriterService.class);

	@Autowired
	@Qualifier("GH")
	// @Qualifier("ORS")
	// @Qualifier("OSRM")
	private RoutingService routingService;

	private List<Offer> offers;

	private File directory;

	private Agency agency = new Agency();

	private AgencyAndId agencyAndId;

	private List<Stop> stops = new ArrayList<>();

	private Stop stop;

	private List<Route> routes = new ArrayList<>();

	private List<Trip> trips = new ArrayList<>();

	private Request request = new Request();

	private Response response;

	public void writeProviderInfoAsGTFS(final File directory) {
		init(null, directory);

		setAgency();

		writeFile(Arrays.asList(agency), "agency.txt");
		writeFile(getFeedInfo(), "feed_info.txt");
	}

	public void writeOfferDataAsGTFS(final List<Offer> offers, final File directory) {
		if (offers.size() == 0) {
			return;
		}

		init(offers, directory);

		setRoutingInformation();

		if (this.offers.size() == 0) {
			return;
		}

		setAgency();
		setStops();
		setRoutes();
		setTrips();

		addToFile(stops, "stops.txt");
		addToFile(routes, "routes.txt");
		addToFile(getCalendars(), "calendar.txt");
		addToFile(trips, "trips.txt");
		addToFile(getStopTimes(), "stop_times.txt");

		LOG.info("Saved {} offers as GTFS", trips.size());
	}

	private void init(final List<Offer> offers, final File directory) {
		this.offers = offers;
		this.directory = directory;
	}

	private void setRoutingInformation() {
		Offer offer;
		int timeInSeconds;
		Place from, to;
		Response response;
		boolean remove;
		for (int i = 0; i < offers.size(); i++) {
			offer = offers.get(i);
			remove = false;
			timeInSeconds = offer.getStartTime().toSecondOfDay();
			from = offer.getOrigin();
			from.setTimeInSeconds(timeInSeconds);
			if (offer.getIntermediatePlaces() != null) {
				for (int j = 0; j < offer.getIntermediatePlaces().size(); j++) {
					to = offer.getIntermediatePlaces().get(j);
					response = getRouting(from, to);
					if (response == null || response.getDuration() == null) {
						remove = true;
						break;
					} else {
						timeInSeconds += response.getDuration().intValue();
						to.setTimeInSeconds(timeInSeconds);
					}
					from = to;
				}
			}
			if (!remove) {
				to = offer.getDestination();
				response = getRouting(from, to);
				if (response == null || response.getDuration() == null) {
					remove = true;
				} else {
					timeInSeconds += response.getDuration().intValue();
					offer.getDestination().setTimeInSeconds(timeInSeconds);
				}
			}
			if (remove) {
				offers.remove(i);
				i--;
				LOG.info("Remove Offer with missing routing info. Offer id is: " + offer.getId());
				continue;
			}
		}
	}

	private Response getRouting(Place from, Place to) {
		request.setOrigin(from.getGeoCoordinates());
		request.setDestination(to.getGeoCoordinates());
		response = routingService.calculateRoute(request);
		return response;
	}

	private <T> void addToFile(List<T> list, final String f) {
		if (list.size() == 0) {
			return;
		}

		GtfsReader reader = new GtfsReader();
		GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
		try {
			reader.setInputLocation(directory);
			reader.setEntityStore(dao);
			Collection<T> oldColllection = (Collection<T>) dao.getAllEntitiesForType(list.get(0).getClass());
			if (oldColllection != null && oldColllection.size() > 0) {
				list.addAll(oldColllection);
			}
		} catch (MissingRequiredEntityException e) {
			LOG.info("File {} has no entries yet" + f);
		} catch (IOException e) {
			LOG.error("Problem getting entries out of file {}: {}" + f, e.getMessage());
		}
		try {
			if (dao != null) {
				dao.close();
			}
		} catch (Exception e) {
			LOG.error("Problem closing GtfsRelationalDaoImpl: " + e.getMessage());
		}
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			LOG.error("Problem closing GtfsReader: " + e.getMessage());
		}
		writeFile(list, f);
	}

	private <T> void writeFile(final List<T> list, final String f) {
		GtfsWriter writer = new GtfsWriter();
		writer.setOutputLocation(directory);
		for (T item : list) {
			writer.handleEntity(item);
		}
		try {
			writer.close();
		} catch (IOException e) {
			LOG.error("Could not write {} file: {}" + f, e.getMessage());
		}
	}

	private void setAgency() {
		agency.setId("agency_1");
		agency.setName("ride2go");
		agency.setUrl("http://www.ride2go.com");
		agency.setTimezone("Europe/Berlin");
		agency.setLang("de");
	}

	private List<FeedInfo> getFeedInfo() {
		FeedInfo feedInfo = new FeedInfo();
		feedInfo.setPublisherName("ride2go");
		feedInfo.setPublisherUrl("http://www.ride2go.com");
		// 'default_lang' field is missing
		feedInfo.setLang("de");
		// NPE if optional dates are not set
		feedInfo.setStartDate(getToday());
		feedInfo.setEndDate(getOneMonthFromToday());
		feedInfo.setVersion("1");
		return Arrays.asList(feedInfo);
	}

	private void setStops() {
		Place place;
		for (Offer offer : offers) {
			place = offer.getOrigin();
			stop = getStop(place);
			stops.add(stop);
			if (offer.getIntermediatePlaces() != null) {
				for (Place intermediatePlace : offer.getIntermediatePlaces()) {
					stop = getStop(intermediatePlace);
					stops.add(stop);
				}
			}
			place = offer.getDestination();
			stop = getStop(place);
			stops.add(stop);
		}
	}

	private Stop getStop(Place place) {
		stop = new Stop();
		stop.setId(getAgencyAndId("place_" + place.getId()));
		stop.setName(place.getAddress());
		stop.setLat(place.getGeoCoordinates().getLatitude());
		stop.setLon(place.getGeoCoordinates().getLongitude());
		return stop;
	}

	private void setRoutes() {
		Route route;
		String name;
		for (Offer offer : offers) {
			name = offer.getOrigin().getAddress() + " -> " + offer.getDestination().getAddress();

			route = new Route();
			route.setId(getAgencyAndId("trip_" + offer.getId()));
			route.setAgency(agency);
			route.setLongName(name);
			route.setType(MISCELLANEOUS_SERVICE);
			// todo
			// route.setUrl(url);
			routes.add(route);
		}
	}

	private List<ServiceCalendar> getCalendars() {
		List<ServiceCalendar> calendars = new ArrayList<>();
		ServiceCalendar calendar;
		Recurring recurring;
		ServiceDate startDate, endDate;
		for (Offer offer : offers) {
			calendar = new ServiceCalendar();
			calendar.setServiceId(getAgencyAndId("trip_" + offer.getId()));

			if (offer.getRecurring() != null) {
				if (offer.getStartDate() != null) {
					LocalDate startdate = offer.getStartDate();
					startDate = getByDate(startdate);
				} else {
					startDate = getToday();
				}
				endDate = getOneMonthFromToday();
				calendar.setStartDate(startDate);
				calendar.setEndDate(endDate);

				recurring = offer.getRecurring();
				calendar.setMonday(recurring.isMonday() ? 1 : 0);
				calendar.setTuesday(recurring.isTuesday() ? 1 : 0);
				calendar.setWednesday(recurring.isWednesday() ? 1 : 0);
				calendar.setThursday(recurring.isThursday() ? 1 : 0);
				calendar.setFriday(recurring.isFriday() ? 1 : 0);
				calendar.setSaturday(recurring.isSaturday() ? 1 : 0);
				calendar.setSunday(recurring.isSunday() ? 1 : 0);
			} else {
				LocalDate startdate = offer.getStartDate();
				startDate = getByDate(startdate);
				endDate = getByDate(startdate);
				calendar.setStartDate(startDate);
				calendar.setEndDate(endDate);

				calendar.setMonday(1);
				calendar.setTuesday(1);
				calendar.setWednesday(1);
				calendar.setThursday(1);
				calendar.setFriday(1);
				calendar.setSaturday(1);
				calendar.setSunday(1);
			}
			calendars.add(calendar);
		}
		return calendars;
	}

	private void setTrips() {
		Trip trip;
		Offer offer;
		AgencyAndId id;
		String tripHeadsign;
		for (int i = 0; i < offers.size(); i++) {
			offer = offers.get(i);
			id = getAgencyAndId("trip_" + offer.getId());
			tripHeadsign = offer.getDestination().getAddress();

			trip = new Trip();
			trip.setId(id);
			trip.setRoute(routes.get(i));
			trip.setServiceId(id);
			trip.setDirectionId(ONE_DIRECTION);
			trip.setTripHeadsign(tripHeadsign);
			trips.add(trip);
		}
	}

	private List<StopTime> getStopTimes() {
		List<StopTime> stopTimes = new ArrayList<>();
		StopTime stopTime;
		Offer offer;
		int stopSequenzIndex;
		int stopIndex = 0;
		for (int i = 0; i < offers.size(); i++) {
			offer = offers.get(i);
			stopSequenzIndex = 0;
			stopTime = getStopTime(i, ++stopSequenzIndex, stopIndex++, offer.getOrigin().getTimeInSeconds());
			stopTimes.add(stopTime);
			if (offer.getIntermediatePlaces() != null) {
				for (Place intermediatePlace : offer.getIntermediatePlaces()) {
					stopTime = getStopTime(i, ++stopSequenzIndex, stopIndex++, intermediatePlace.getTimeInSeconds());
					stopTimes.add(stopTime);
				}
			}
			stopTime = getStopTime(i, ++stopSequenzIndex, stopIndex++, offer.getDestination().getTimeInSeconds());
			stopTimes.add(stopTime);
		}
		return stopTimes;
	}

	private StopTime getStopTime(int i, int stopSequenzIndex, int stopIndex, int timeInSeconds) {
		StopTime stopTime = new StopTime();
		stopTime.setTrip(trips.get(i));
		stopTime.setStopSequence(stopSequenzIndex);
		stopTime.setStop(stops.get(stopIndex));
		stopTime.setArrivalTime(timeInSeconds);
		stopTime.setDepartureTime(timeInSeconds);
		stopTime.setTimepoint((stopSequenzIndex == 1) ? EXACT_TIMEPOINT : APPROXIMATE_TIMEPOINT);
		return stopTime;
	}

	private AgencyAndId getAgencyAndId(String id) {
		agencyAndId = new AgencyAndId();
		agencyAndId.setAgencyId(agency.getId());
		agencyAndId.setId(id);
		return agencyAndId;
	}

	private ServiceDate getToday() {
		return new ServiceDate(TODAY.getYear(), TODAY.getMonthValue(), TODAY.getDayOfMonth());
	}

	private ServiceDate getOneMonthFromToday() {
		return new ServiceDate(ONE_MONTH_FROM_TODAY.getYear(), ONE_MONTH_FROM_TODAY.getMonthValue(),
				ONE_MONTH_FROM_TODAY.getDayOfMonth());
	}

	private ServiceDate getByDate(LocalDate date) {
		return new ServiceDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
	}
}
