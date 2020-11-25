package com.ride2go.ridetogtfsconverter.conversion;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.ONE_DAY_IN_SECONDS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
			offers.add(offer);
		}
		return offers;
	}

	private List<Place> getPlaces(final EntityTrip trip) {
		List<Place> places = new ArrayList<>();
		List<EntityRouting> routings = trip.getRoutings();
		int last = routings.size() - 1;
		Place originPlace = getPlace(routings.get(last).getOrigin(), null);
		originPlace.setTimeInSeconds(trip.getStarttime().toSecondOfDay());
		previousTimeInSeconds = originPlace.getTimeInSeconds();
		places.add(originPlace);
		if (routings.size() > 1) {
			for (int i = 0; i < routings.size() - 2; i++) {
				Place intermediatePlace = getPlace(routings.get(i).getDestination(),
						routings.get(i + 1).getOrigin());
				places.add(intermediatePlace);
			}
		}
		Place destinationPlace;
		if (last != 0) {
			destinationPlace = getPlace(routings.get(last).getDestination(), routings.get(last - 1).getDestination());
		} else {
			destinationPlace = getPlace(routings.get(last).getDestination(), null);
		}
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

	private Place getPlace(final EntityRoutingPlace entityRoutingPlace,
			final EntityRoutingPlace entityRoutingPlaceDouble) {
		GeoCoordinates geoCoordinates = new GeoCoordinates(entityRoutingPlace.getLat(), entityRoutingPlace.getLon());

		Place place = new Place();
		place.setId(entityRoutingPlace.getPlaceId());
		place.setGeoCoordinates(geoCoordinates);
		place.setAddress(entityRoutingPlace.getAddress());
		place.setTimeInSeconds(getTimeInSeconds(entityRoutingPlace, entityRoutingPlaceDouble));
		return place;
	}

	private Integer getTimeInSeconds(final EntityRoutingPlace entityRoutingPlace,
			final EntityRoutingPlace entityRoutingPlaceDouble) {
		LocalTime stoptime = entityRoutingPlace.getStoptime();
		if (entityRoutingPlaceDouble != null && stoptime == null) {
			stoptime = entityRoutingPlaceDouble.getStoptime();
		}
		if (stoptime != null) {
			int stoptimeInSeconds = stoptime.toSecondOfDay();
			while (stoptimeInSeconds < previousTimeInSeconds) {
				stoptimeInSeconds += ONE_DAY_IN_SECONDS;
			}
			previousTimeInSeconds = stoptimeInSeconds;
			return stoptimeInSeconds;
		}
		return null;
	}
}
