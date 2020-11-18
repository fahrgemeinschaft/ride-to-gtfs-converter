package com.ride2go.ridetogtfsconverter.ridesdata;

import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.FEED_END_DATE;
import static com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter.FEED_START_DATE;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.conversion.OfferConverter;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.repository.TripRepository;
import com.ride2go.ridetogtfsconverter.validation.TripValidator;

@Service
public class DBReaderService implements ReaderService {

	private static final Logger LOG = LoggerFactory.getLogger(DBReaderService.class);

	@Autowired
	private OfferConverter offerConverter;

	@Autowired
	private TripValidator tripValidator;

	@Autowired
	private TripRepository tripRepository;

	@Value("${custom.gtfs.trips.use-time-period}")
    private boolean useTimePeriod;

	private static final List<DayOfWeek> FEED_TIME_PERIOD_WEEK_DAYS = getFeedTimePeriodWeekDays();

	private int size;

	public List<Offer> getOffersByUserId(final String userId) {
		List<EntityTrip> trips = getValidRelevantAndOngoingUserOffers(userId);
		List<Offer> offers = offerConverter.fromTripToOffer(trips);
		if (size > 0) {
			LOG.info("Using {} valid, relevant and ongoing user offers having all required fields", trips.size());
		}
		return offers;
	}

	@Async
	public CompletableFuture<List<Offer>> getOffersByUserIdAsync(final String userId) {
		return CompletableFuture.completedFuture(getOffersByUserId(userId));
	}

	public List<Offer> getOfferPage(final Pageable page) {
		List<EntityTrip> trips = getValidRelevantAndOngoingOffers(page);
		List<Offer> offers = offerConverter.fromTripToOffer(trips);
		if (size > 0) {
			LOG.info("Using {} valid, relevant and ongoing user offers having all required fields", trips.size());
		}
		return offers;
	}

	@Async
	public CompletableFuture<List<Offer>> getOfferPageAsync(final Pageable page) {
		return CompletableFuture.completedFuture(getOfferPage(page));
	}

	private List<EntityTrip> getValidRelevantAndOngoingUserOffers(final String userId) {
		List<EntityTrip> trips = getOfferList(userId);
		size = trips.size();
		LOG.info("Found {} trips in the database for user: {}", size, userId);
		tripValidator.validTrips(trips);
		ongoingTrips(trips);
		ongoingMissingreoccurs(trips);
		return trips;
	}

	private List<EntityTrip> getValidRelevantAndOngoingOffers(final Pageable page) {
		List<EntityTrip> trips = getOfferList(page);
		size = trips.size();
		LOG.info("Found {} trips in the database for page {}", size, page.getPageNumber());
		tripValidator.validTrips(trips);
		ongoingTrips(trips);
		ongoingMissingreoccurs(trips);
		return trips;
	}

	private static final String TRIPTYPE_OFFER = "offer";

	private static final Integer RELEVANCE_SEARCHABLE = 10;

	private List<EntityTrip> getOfferList(final String userId) {
		List<EntityTrip> offers = null;
		try {
			offers = tripRepository.findByUserIdAndTriptypeAndRelevance(userId, TRIPTYPE_OFFER, RELEVANCE_SEARCHABLE);
		} catch (JpaObjectRetrievalFailureException e) {
			LOG.info("JPA object retrieval failure when getting user trips: " + e.getMessage());
		}
		return (offers == null) ? new ArrayList<>() : offers;
	}

	private List<EntityTrip> getOfferList(final Pageable page) {
		List<EntityTrip> offers = null;
		try {
			offers = tripRepository.findByTriptypeAndRelevance(TRIPTYPE_OFFER, RELEVANCE_SEARCHABLE, page);
		} catch (JpaObjectRetrievalFailureException e) {
			LOG.error("JPA object retrieval failure when getting trip page: " + e.getMessage());
		}
		return (offers == null) ? new ArrayList<>() : offers;
	}

	private void ongoingTrips(List<EntityTrip> trips) {
		EntityTrip trip;
		for (int i = 0; i < trips.size(); i++) {
			trip = trips.get(i);
			if (trip.getStartdate() != null && !trip.getStartdate().isBefore(FEED_START_DATE)) {
				// ongoing
			} else if (trip.getReoccurs() != null && trip.getReoccurs().doesReoccur()) {
				// ongoing
			} else {
				LOG.info("Remove expired Trip with id: " + trip.getTripId());
				trips.remove(i);
				i--;
			}
		}
		if (useTimePeriod) {
			for (int i = 0; i < trips.size(); i++) {
				trip = trips.get(i);
				if (trip.getStartdate() != null && !trip.getStartdate().isAfter(FEED_END_DATE)) {
					// within period
				} else if (trip.getReoccurs() != null && !Collections.disjoint(FEED_TIME_PERIOD_WEEK_DAYS, trip.getReoccurs().getReoccurDays())) {
					// within period
				} else {
					LOG.info("Remove Trip after feed time period with id: " + trip.getTripId());
					trips.remove(i);
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

	private void ongoingMissingreoccurs(List<EntityTrip> trips) {
		List<ZonedDateTime> missingreoccurs;
		LocalDate missingreoccursItem;
		for (EntityTrip trip : trips) {
			missingreoccurs = trip.getMissingreoccurs();
			if (missingreoccurs != null) {
				for (int i = 0; i < missingreoccurs.size(); i++) {
					missingreoccursItem = missingreoccurs.get(i).toLocalDate();
					if (missingreoccursItem.isBefore(FEED_START_DATE)
							|| (useTimePeriod && missingreoccursItem.isAfter(FEED_END_DATE))) {
						LOG.info("Remove missingreoccurs day: " + missingreoccursItem);
						missingreoccurs.remove(i);
						i--;
					}
				}
			}
		}
	}
}
