package com.ride2go.ridetogtfsconverter.conversion;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.ONE_DAY_IN_SECONDS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
		int last;
		for (EntityTrip trip : trips) {
			Offer offer = new Offer();
			offer.setId(trip.getTripId());
			offer.setStartDate(trip.getStartdate());
			last = trip.getRoutings().size() - 1;
			Place originPlace = getPlace(trip.getRoutings().get(last).getOrigin());
			originPlace.setTimeInSeconds(trip.getStarttime().toSecondOfDay());
			previousTimeInSeconds = originPlace.getTimeInSeconds();
			List<Place> places = new ArrayList<>();
			places.add(originPlace);
			if (trip.getRoutings().size() > 1) {
				for (int i = 0; i < trip.getRoutings().size() - 2; i++) {
					Place intermediatePlace = getPlace(trip.getRoutings().get(i).getDestination());
					places.add(intermediatePlace);
				}
			}
			Place destinationPlace = getPlace(trip.getRoutings().get(last).getDestination());
			places.add(destinationPlace);
			offer.setPlaces(places);
			if (trip.getReoccurs().doesReoccur()) {
				Recurring recurring = new Recurring();
				recurring.setMonday(trip.getReoccurs().getMo());
				recurring.setTuesday(trip.getReoccurs().getTu());
				recurring.setWednesday(trip.getReoccurs().getWe());
				recurring.setThursday(trip.getReoccurs().getTh());
				recurring.setFriday(trip.getReoccurs().getFr());
				recurring.setSaturday(trip.getReoccurs().getSa());
				recurring.setSunday(trip.getReoccurs().getSu());
				offer.setRecurring(recurring);
			}
			offer.setMissingreoccurs(trip.getMissingreoccurs());
			offers.add(offer);
		}
		return offers;
	}

	private Place getPlace(final EntityRoutingPlace entityRoutingPlace) {
		GeoCoordinates geoCoordinates = new GeoCoordinates(entityRoutingPlace.getLat(), entityRoutingPlace.getLon());

		Place place = new Place();
		place.setId(entityRoutingPlace.getPlaceId());
		place.setGeoCoordinates(geoCoordinates);
		place.setAddress(entityRoutingPlace.getAddress());

		LocalTime stoptime = entityRoutingPlace.getStoptime();
		if (stoptime != null) {
			int stoptimeInSeconds = stoptime.toSecondOfDay();
			while (stoptimeInSeconds < previousTimeInSeconds) {
				stoptimeInSeconds += ONE_DAY_IN_SECONDS;
			}
			place.setTimeInSeconds(stoptimeInSeconds);
			previousTimeInSeconds = stoptimeInSeconds;
		}
		return place;
	}
}
