package com.ride2go.ridetogtfsconverter.validation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.conversion.JSONConverter;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRouting;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRoutingPlace;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;

@Service
public class TripValidator {

	private static final Logger LOG = LoggerFactory.getLogger(TripValidator.class);

	@Autowired
	private JSONConverter jsonConverter;

	public void validTrips(List<EntityTrip> trips) {
		EntityTrip trip;
		List<EntityRouting> routings;
		EntityRoutingPlace origin, destination;
		boolean remove;
		for (int i = 0; i < trips.size(); i++) {
			trip = trips.get(i);
			routings = trip.getRoutings();
			if (trip.getTripId() == null || trip.getTripId().trim().isEmpty() || !hasRouting(routings)) {
				LOG.info("Remove invalid Trip without tripId or routings: " + jsonConverter.toJSONString(trip));
				trips.remove(i);
				i--;
				continue;
			}
			remove = false;
			for (EntityRouting routing : routings) {
				origin = routing.getOrigin();
				destination = routing.getDestination();
				if (!isPlace(origin) || !isPlace(destination)) {
					LOG.info("Remove invalid Trip with incomplete origin or destination in at least one routing: "
							+ jsonConverter.toJSONString(trip));
					remove = true;
					break;
				}
			}
			if (!remove) {
				for (int j = 0; j < routings.size(); j++) {
					if (routings.get(j).getOrigin().getLat().doubleValue() == routings.get(j).getDestination().getLat().doubleValue()
							&& routings.get(j).getOrigin().getLon().doubleValue() == routings.get(j).getDestination().getLon().doubleValue()) {
						LOG.info("Remove invalid Trip with origin and destination are equal in at least one routing: "
								+ jsonConverter.toJSONString(trip));
						remove = true;
						break;
					}
				}
			}
			if (!remove) {
				for (int j = 1; j < routings.size() - 1; j++) {
					if (routings.get(j).getOrigin().getLat().doubleValue() != routings.get(j - 1).getDestination().getLat().doubleValue()
							|| routings.get(j).getOrigin().getLon().doubleValue() != routings.get(j - 1).getDestination().getLon().doubleValue()) {
						LOG.info("Remove invalid Trip with destination of one routing is not the same as origin of the next routing: "
										+ jsonConverter.toJSONString(trip));
						remove = true;
						break;
					}
				}
			}
			if (remove) {
				trips.remove(i);
				i--;
				continue;
			}
			if (routings.size() > 1) {
				if (routings.get(0).getOrigin().getLat().doubleValue() != routings.get(routings.size() - 1).getOrigin().getLat().doubleValue()
						|| routings.get(0).getOrigin().getLon().doubleValue() != routings.get(routings.size() - 1).getOrigin().getLon().doubleValue()) {
					LOG.info("Remove invalid Trip with origin of the first routing is not the same as origin of the last routing: "
									+ jsonConverter.toJSONString(trip));
					trips.remove(i);
					i--;
					continue;
				}
				if (routings.get(routings.size() - 2).getDestination().getLat().doubleValue() != routings.get(routings.size() - 1).getDestination().getLat().doubleValue()
						|| routings.get(routings.size() - 2).getDestination().getLon().doubleValue() != routings.get(routings.size() - 1).getDestination().getLon().doubleValue()) {
					LOG.info("Remove invalid Trip with destination of the second last routing is not the same as destination of the last routing: "
									+ jsonConverter.toJSONString(trip));
					trips.remove(i);
					i--;
					continue;
				}
				if (routings.size() == 2) {
					LOG.info("Remove invalid Trip with two routings: " + jsonConverter.toJSONString(trip));
					trips.remove(i);
					i--;
					continue;
				}
			}
			if (trip.getStarttime() == null) {
				LOG.info("Remove invalid Trip without starttime: " + jsonConverter.toJSONString(trip));
				trips.remove(i);
				i--;
				continue;
			}
		}
	}

	private boolean hasRouting(final List<EntityRouting> routings) {
		if (routings != null && routings.size() > 0) {
			return true;
		}
		return false;
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
}
