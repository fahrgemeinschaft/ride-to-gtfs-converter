package com.ride2go.ridetogtfsconverter.ridesdata;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.YESTERDAY;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	public List<Offer> getOffersByUserId(final String userId) {
		List<EntityTrip> trips = getValidRelevantAndOngoingUserOffers(userId);
		List<Offer> offers = offerConverter.fromTripToOffer(trips);
		LOG.info("Using {} valid, relevant and ongoing user offers having all required fields", trips.size());
		return offers;
	}

	private List<EntityTrip> getValidRelevantAndOngoingUserOffers(final String userId) {
		List<EntityTrip> trips = getOfferList(userId);
		LOG.info("Found {} trips in the database for user: {}", trips.size(), userId);
		tripValidator.validTrips(trips);
		ongoingTrips(trips);
		return trips;
	}

	private List<EntityTrip> getOfferList(final String userId) {
		List<EntityTrip> offers = tripRepository.findByUserIdAndTriptypeAndRelevance(userId, "offer", 10);
		if (offers == null) {
			return new ArrayList<>();
		}
		return offers;
	}

	private void ongoingTrips(List<EntityTrip> trips) {
		EntityTrip trip;
		for (int i = 0; i < trips.size(); i++) {
			trip = trips.get(i);
			if (trip.getStartdate() != null && trip.getStartdate().isAfter(YESTERDAY)) {
				// ongoing
			} else if (trip.getReoccurs() != null && trip.getReoccurs().doesReoccur()) {
				// ongoing
			} else {
				LOG.info("Remove expired Trip with id: " + trip.getTripId());
				trips.remove(i);
				i--;
				continue;
			}
		}
	}
}
