package com.ride2go.ridetogtfsconverter.conversion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRoutingPlace;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.model.item.Recurring;

@Service
public class OfferConverter {

	public List<Offer> fromTripToOffer(final List<EntityTrip> trips) {
		List<Offer> offers = new ArrayList<>();
		Offer offer;
		Place place;
		int last;
		List<Place> places;
		Recurring recurring;
		for (EntityTrip trip : trips) {
			offer = new Offer();
			offer.setId(trip.getTripId());
			offer.setStartDate(trip.getStartdate());
			offer.setStartTime(trip.getStarttime());
			last = trip.getRoutings().size() - 1;
			place = getPlace(trip.getRoutings().get(last).getOrigin());
			offer.setOrigin(place);
			place = getPlace(trip.getRoutings().get(last).getDestination());
			offer.setDestination(place);
			if (trip.getRoutings().size() > 1) {
				places = new ArrayList<>();
				for (int i = 0; i < trip.getRoutings().size() - 2; i++) {
					place = getPlace(trip.getRoutings().get(i).getDestination());
					places.add(place);
				}
				offer.setIntermediatePlaces(places);
			}
			if (trip.getReoccurs().doesReoccur()) {
				recurring = new Recurring();
				recurring.setMonday(trip.getReoccurs().getMo());
				recurring.setTuesday(trip.getReoccurs().getTu());
				recurring.setWednesday(trip.getReoccurs().getWe());
				recurring.setThursday(trip.getReoccurs().getTh());
				recurring.setFriday(trip.getReoccurs().getFr());
				recurring.setSaturday(trip.getReoccurs().getSa());
				recurring.setSunday(trip.getReoccurs().getSu());
				offer.setRecurring(recurring);
			}
			offers.add(offer);
		}
		return offers;
	}

	private Place getPlace(EntityRoutingPlace entityRoutingPlace) {
		GeoCoordinates geoCoordinates = new GeoCoordinates(entityRoutingPlace.getLat(), entityRoutingPlace.getLon());
		
		Place place = new Place();
		place.setId(entityRoutingPlace.getPlaceId());
		place.setGeoCoordinates(geoCoordinates);
		place.setAddress(entityRoutingPlace.getAddress());
		return place;
	}
}
