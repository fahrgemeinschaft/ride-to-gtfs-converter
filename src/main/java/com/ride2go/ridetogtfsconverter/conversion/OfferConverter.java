package com.ride2go.ridetogtfsconverter.conversion;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.ONE_DAY_IN_SECONDS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityReoccurs;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRouting;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRoutingPlace;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.model.item.Recurring;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OfferConverter {

	private static final Logger LOG = LoggerFactory.getLogger(OfferConverter.class);

	private int previousTimeInSeconds;

	public List<Offer> fromTripToOffer(final List<EntityTrip> trips) {
		List<Offer> offers = new ArrayList<>();
		for (EntityTrip trip : trips) {
			Offer offer = new Offer();
			offer.setId(trip.getTripId());
			offer.setStartDate(trip.getStartdate());
			offer.setPlaces(getPlaces(trip));
			offer.setRecurring(getRecurring(trip));
			offer.setMissingreoccurs(trip.getMissingreoccurs());
			if (offerHasNoInvalidStopTime(offer)) {
				offers.add(offer);
			} else {
				LOG.debug("Remove invalid Trip with wrong stoptimes: " + trip.getTripId());
			}
		}
		return offers;
	}

	private List<Place> getPlaces(final EntityTrip trip) {
		List<EntityRouting> routings = trip.getRoutings();
		List<Place> places = new ArrayList<>();
		Place originPlace = getPlace(routings.get(0).getOrigin());
		originPlace.setTimeInSeconds(trip.getStarttime().toSecondOfDay());
		previousTimeInSeconds = originPlace.getTimeInSeconds();
		places.add(originPlace);
		for (int i = 1; i < routings.size(); i++) {
			Place intermediatePlace = getPlace(routings.get(i).getDestination());
			places.add(intermediatePlace);
		}
		Place destinationPlace = getPlace(routings.get(0).getDestination());
		places.add(destinationPlace);
		return places;
	}

	private Recurring getRecurring(final EntityTrip trip) {
		EntityReoccurs reoccurs = trip.getReoccurs();
		if (reoccurs.doesReoccur()) {
			Recurring recurring = new Recurring();
			recurring.setMonday(reoccurs.getMo());
			recurring.setTuesday(reoccurs.getTu());
			recurring.setWednesday(reoccurs.getWe());
			recurring.setThursday(reoccurs.getTh());
			recurring.setFriday(reoccurs.getFr());
			recurring.setSaturday(reoccurs.getSa());
			recurring.setSunday(reoccurs.getSu());
			return recurring;
		}
		return null;
	}

	private Place getPlace(final EntityRoutingPlace entityRoutingPlace) {
		GeoCoordinates geoCoordinates = new GeoCoordinates(entityRoutingPlace.getLat(), entityRoutingPlace.getLon());

		Place place = new Place();
		place.setId(entityRoutingPlace.getPlaceId());
		place.setGeoCoordinates(geoCoordinates);
		place.setAddress(entityRoutingPlace.getAddress());
		place.setTimeInSeconds(getTimeInSeconds(entityRoutingPlace));
		return place;
	}

	private Integer getTimeInSeconds(final EntityRoutingPlace entityRoutingPlace) {
		LocalTime stoptime = entityRoutingPlace.getStoptime();
		if (stoptime != null) {
			int stoptimeInSeconds = stoptime.toSecondOfDay();
			while (stoptimeInSeconds < previousTimeInSeconds) {
				stoptimeInSeconds += ONE_DAY_IN_SECONDS;
			}
			if (stoptimeInSeconds - previousTimeInSeconds > ONE_DAY_IN_SECONDS / 2) {
				return -1;
			}
			previousTimeInSeconds = stoptimeInSeconds;
			return stoptimeInSeconds;
		}
		return null;
	}

	private boolean offerHasNoInvalidStopTime(final Offer offer) {
		for (Place place : offer.getPlaces()) {
			if (place.getTimeInSeconds() != null && place.getTimeInSeconds().intValue() == -1) {
				return false;
			}
		}
		return true;
	}
}
