package com.ride2go.ridetogtfsconverter.ridesdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.conversion.OfferConverter;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.repository.TripRepository;
import com.ride2go.ridetogtfsconverter.validation.Constraints;
import com.ride2go.ridetogtfsconverter.validation.TripValidator;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DBReaderService implements ReaderService {

	private static final Logger LOG = LoggerFactory.getLogger(DBReaderService.class);

	private static final String TRIPTYPE_OFFER = "offer";

	private static final Integer RELEVANCE_SEARCHABLE = 10;

	@Autowired
	private TripRepository tripRepository;

	@Autowired
	private OfferConverter offerConverter;

	@Autowired
	private TripValidator tripValidator;

	@Autowired
	Constraints constraints;

	private int size;

	public List<Offer> getOffersByUserId(String userId) {
		List<EntityTrip> trips = getValidRelevantAndOngoingUserOffers(userId);
		List<Offer> offers = convert(trips);
		constraints.withinArea(offers);
		return offers;
	}

	public long getOfferByTriptypeAndRelevanceCount() {
		return tripRepository.countByTriptypeAndRelevance(TRIPTYPE_OFFER, RELEVANCE_SEARCHABLE);
	}

	@Async
	public CompletableFuture<List<Offer>> getOfferPageAsync(final Pageable page) {
		return CompletableFuture.completedFuture(
				getOfferPage(page));
	}

	private List<Offer> getOfferPage(final Pageable page) {
		List<EntityTrip> trips = getValidRelevantAndOngoingOffers(page);
		List<Offer> offers = convert(trips);
		constraints.withinArea(offers);
		return offers;
	}

	private List<EntityTrip> getValidRelevantAndOngoingUserOffers(String userId) {
		List<EntityTrip> trips = getOfferList(userId);
		size = trips.size();
		LOG.info("Found {} trips in the database for user: {}", size, userId);
		check(trips);
		return trips;
	}

	private List<EntityTrip> getValidRelevantAndOngoingOffers(final Pageable page) {
		List<EntityTrip> trips = getOfferList(page);
		size = trips.size();
		LOG.info("Found {} trips in the database for page {}", size, page.getPageNumber());
		check(trips);
		return trips;
	}

	private List<Offer> convert(List<EntityTrip> trips) {
		List<Offer> offers = offerConverter.fromTripToOffer(trips);
		if (size > 0) {
			LOG.info("Using {} valid, relevant and ongoing offers having all required fields", trips.size());
		}
		return offers;
	}

	private List<EntityTrip> getOfferList(String userId) {
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

	private void check(List<EntityTrip> trips) {
		tripValidator.validTrips(trips);
		constraints.ongoingTrips(trips);
		constraints.ongoingMissingreoccurs(trips);
	}
}
