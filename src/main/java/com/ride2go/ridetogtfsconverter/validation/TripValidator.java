package com.ride2go.ridetogtfsconverter.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.conversion.JSONConverter;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRouting;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRoutingPlace;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.util.EntityRoutingComparator;

@Service
public class TripValidator {

	private static final Logger LOG = LoggerFactory.getLogger(TripValidator.class);

	private static final EntityRoutingComparator COMPARATOR = new EntityRoutingComparator();

	@Autowired
	private JSONConverter jsonConverter;

	public void validTrips(List<EntityTrip> trips) {
		EntityTrip trip;
		String print;
		List<EntityRouting> routings;
		boolean remove;
		for (int i = 0; i < trips.size(); i++) {
			trip = trips.get(i);
			print = jsonConverter.toJSONString(trip);
			routings = trip.getRoutings();
			if (trip.getTripId() == null || trip.getTripId().trim().isEmpty()) {
				LOG.debug("Remove invalid Trip without tripId: " + print);
				trips.remove(i);
				i--;
				continue;
			}
			if (!hasCompleteRoutingIdx(routings)) {
				if (trip.getRoutings() == null || trip.getRoutings().isEmpty()) {
					LOG.debug("Remove invalid Trip without routings: " + print);
				} else {
					LOG.debug("Remove invalid Trip with incomplete routing idx settings: " + print);
				}
				trips.remove(i);
				i--;
				continue;
			}
			remove = false;
			for (EntityRouting routing : routings) {
				if (equalOriginAndDestination(routing)) {
					LOG.debug("Remove invalid Trip with origin and destination are equal in at least one routing: " + print);
					remove = true;
					break;
				}
			}
			if (!remove) {
				for (int j = 1; j < routings.size(); j++) {
					if (!equalPlaces(routings.get(0).getOrigin(), routings.get(j).getOrigin())) {
						LOG.debug("Remove invalid Trip with origin not the same in all routings with positive idx: " + print);
						remove = true;
						break;
					}
				}
			}
			if (!remove) {
				for (int j = 1; j < routings.size(); j++) {
					if (equalPlaces(routings.get(0).getOrigin(), routings.get(j).getDestination())) {
						LOG.debug("Remove invalid Trip with origin also in destination for at least one routing with positive idx: " + print);
						remove = true;
						break;
					}
				}
			}
			if (!remove) {
				for (int j = 1; j < routings.size(); j++) {
					for (int k = j + 1; k < routings.size(); k++) {
						if (equalPlaces(routings.get(j).getDestination(), routings.get(k).getDestination())) {
							LOG.debug("Remove invalid Trip with destination in more than one routing with positive idx: " + print);
							remove = true;
							j = routings.size();
							break;
						}
					}
				}
			}
			if (remove) {
				trips.remove(i);
				i--;
				continue;
			}
			if (trip.getStarttime() == null) {
				LOG.debug("Remove invalid Trip without starttime: " + trip.getTripId());
				trips.remove(i);
				i--;
			} else {
				LOG.debug("Valid Trip: " + print);
			}
		}
	}

	public void withDurationGreaterThanZero(List<Offer> offers) {
		boolean withZeroDuration;
		List<String> timeInSecondsList;
		String timeInSeconds;
		for (int i = 0; i < offers.size(); i++) {
			withZeroDuration = false;
			timeInSecondsList = new ArrayList<>();
			for (Place place : offers.get(i).getPlaces()) {
				if (place.getTimeInSeconds() != null) {
					timeInSeconds = place.getTimeInSeconds().toString();
					if (!timeInSecondsList.contains(timeInSeconds)) {
						timeInSecondsList.add(timeInSeconds);
					} else {
						withZeroDuration = true;
						break;
					}
				}
			}
			if (withZeroDuration) {
				LOG.debug("Remove invalid Trip with zero travel time between stops: "
						+ jsonConverter.toJSONString(offers.get(i)));
				offers.remove(i);
				i--;
			}
		}
	}

	private boolean hasCompleteRoutingIdx(List<EntityRouting> routings) {
		if (routings == null) {
			return false;
		}
		cleanRoutings(routings);
		if (routings.isEmpty()) {
			return false;
		}
		Collections.sort(routings, COMPARATOR);
		if (!idxUnique(routings)) {
			return false;
		}
		final boolean missingIdxZero = checkForMissingIdxZero(routings);
		final boolean idxGaps = checkForIdxGaps(routings);
		fixRoutings(routings, missingIdxZero, idxGaps);
		if (!idxValid(routings)) {
			return false;
		}
		return true;
	}

	private boolean equalOriginAndDestination(final EntityRouting routing) {
		return equalPlaces(routing.getOrigin(), routing.getDestination());
	}

	private boolean equalPlaces(final EntityRoutingPlace place1, final EntityRoutingPlace place2) {
		if (Double.compare(place1.getLat().doubleValue(), place2.getLat().doubleValue()) == 0
				&& Double.compare(place1.getLon().doubleValue(), place2.getLon().doubleValue()) == 0) {
			return true;
		}
		return false;
	}

	private void cleanRoutings(List<EntityRouting> routings) {
		for (int i = 0; i < routings.size(); i++) {
			if (routings.get(i) == null
					|| routings.get(i).getIdx() == null
					|| routings.get(i).getIdx().intValue() < 0
					|| routings.get(i).getOrigin() == null
					|| routings.get(i).getDestination() == null
					|| !isPlace(routings.get(i).getOrigin())
					|| !isPlace(routings.get(i).getDestination())) {
				routings.remove(i);
				i--;
			} else if (equalOriginAndDestination(routings.get(i))) {
				for (int j = 0; j < routings.size(); j++) {
					if (i != j
							&& (equalPlaces(routings.get(i).getOrigin(), routings.get(j).getDestination())
									|| equalPlaces(routings.get(i).getOrigin(), routings.get(j).getOrigin()))) {
						routings.remove(i);
						i--;
						break;
					}
				}
			}
		}
		for (int i = 0; i < routings.size(); i++) {
			for (int j = i + 1; j < routings.size(); j++) {
				if (equalRoutings(routings.get(i), routings.get(j))) {
					routings.remove(j);
					j--;
				}
			}
		}
	}

	private boolean idxUnique(final List<EntityRouting> routings) {
		for (int i = 1; i < routings.size(); i++) {
			if (routings.get(i - 1).getIdx().intValue() == routings.get(i).getIdx().intValue()) {
				return false;
			}
		}
		return true;
	}

	private int[] getIdxArray(final List<EntityRouting> routings) {
		int[] idxArray = new int[routings.size()];
		for (int i = 0; i < routings.size(); i++) {
			idxArray[i] = routings.get(i).getIdx().intValue();
		}
		return idxArray;
	}

	private boolean checkForMissingIdxZero(final List<EntityRouting> routings) {
		return (routings.get(0).getIdx().intValue() != 0) ? true : false;
	}

	private boolean checkForIdxGaps(final List<EntityRouting> routings) {
		for (int i = 0; i < routings.size(); i++) {
			if (routings.get(i).getIdx().intValue() > i) {
				return true;
			}
		}
		return false;
	}

	private boolean idxValid(final List<EntityRouting> routings) {
		for (int i = 0; i < routings.size(); i++) {
			if (routings.get(i).getIdx().intValue() != i) {
				return false;
			}
		}
		return true;
	}

	private void fixRoutings(List<EntityRouting> routings, boolean missingIdxZero, boolean idxGaps) {
		if (missingIdxZero) {
			routings.get(routings.size() - 1).setIdx(0);
			Collections.sort(routings, COMPARATOR);
		}
		if (idxGaps) {
			for (int i = 0; i < routings.size(); i++) {
				if (routings.get(i).getIdx().intValue() > i) {
					routings.get(i).setIdx(i);
				}
			}
		}
	}

	private boolean isPlace(final EntityRoutingPlace place) {
		if (place != null
				&& place.getPlaceId() != null
				&& !place.getPlaceId().trim().isEmpty()
				&& place.getAddress() != null
				&& !place.getAddress().trim().isEmpty()
				&& place.getLat() != null
				&& place.getLon() != null) {
			return true;
		}
		return false;
	}

	private boolean equalRoutings(final EntityRouting routing1, final EntityRouting routing2) {
		if (routing1.getIdx().intValue() == routing2.getIdx().intValue()
				&& equalPlaces(routing1.getOrigin(), routing2.getOrigin())
				&& equalPlaces(routing1.getDestination(), routing2.getDestination())) {
			return true;
		}
		return false;
	}
}
