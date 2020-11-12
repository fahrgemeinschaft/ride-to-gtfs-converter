package com.ride2go.ridetogtfsconverter.routing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;

@Service
public class RoutingHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RoutingHandler.class);

	@Autowired
	// @Qualifier("GH")
	// @Qualifier("ORS")
	@Qualifier("OSRM")
	private RoutingService routingService;

	private Request routingRequest = new Request();

	private Response routingResponse;

	public void setRoutingInformation(List<Offer> offers) {
		Offer offer;
		boolean remove, intermediatePlacesExist;
		Place from, to;
		List<Place> intermediatePlaces;
		int intermediatePlacesSize = 0;
		int[] durations = null;
		for (int i = 0; i < offers.size(); i++) {
			offer = offers.get(i);
			remove = false;
			from = offer.getOrigin();
			intermediatePlaces = offer.getIntermediatePlaces();
			intermediatePlacesExist = intermediatePlaces != null && !intermediatePlaces.isEmpty();
			if (intermediatePlacesExist) {
				intermediatePlacesSize = intermediatePlaces.size();
				durations = new int[intermediatePlacesSize + 1];
				for (int j = 0; j < intermediatePlacesSize; j++) {
					to = intermediatePlaces.get(j);
					getRouting(from, to);
					if (routingHasValidDuration()) {
						durations[j] = routingResponse.getDuration().intValue();
					} else if (to.getTimeInSeconds() == null) {
						remove = true;
						break;
					}
					from = to;
				}
			}
			if (!remove) {
				to = offer.getDestination();
				getRouting(from, to);
				if (routingHasValidDuration()) {
					if (intermediatePlacesExist) {
						durations[intermediatePlacesSize] = routingResponse.getDuration().intValue();
					} else if (to.getTimeInSeconds() == null) {
						to.setTimeInSeconds(from.getTimeInSeconds() + routingResponse.getDuration().intValue());
					}
				} else if (to.getTimeInSeconds() == null) {
					remove = true;
				}
			}
			if (remove) {
				offers.remove(i);
				i--;
				LOG.info("Remove Offer with missing routing info. Offer id is: " + offer.getId());
				continue;
			}
			if (intermediatePlacesExist) {
				setRoutingTimes(offer, intermediatePlacesSize, durations);
			}
		}
	}

	private void getRouting(Place from, Place to) {
		routingRequest.setOrigin(from.getGeoCoordinates());
		routingRequest.setDestination(to.getGeoCoordinates());
		routingResponse = routingService.calculateRoute(routingRequest);
	}

	private boolean routingHasValidDuration() {
		return routingResponse != null
				&& routingResponse.getDuration() != null
				&& routingResponse.getDuration().doubleValue() >= 0;
	}

	private void setRoutingTimes(Offer offer, int intermediatePlacesSize, int[] durations) {
		Place place1 = offer.getOrigin();
		Place place2 = offer.getIntermediatePlaces().get(0);
		Place place3;
		for (int j = 0; j < intermediatePlacesSize - 1; j++) {
			place3 = offer.getIntermediatePlaces().get(j + 1);
			setRoutingTime(place1, place2, place3, durations[j], durations[j + 1]);
			place1 = place2;
			place2 = place3;
		}
		place3 = offer.getDestination();
		setRoutingTime(place1, place2, place3, durations[intermediatePlacesSize - 1],
				durations[intermediatePlacesSize]);
		setRoutingTime(place2, place3, new Place(), durations[intermediatePlacesSize], 0);
	}

	private void setRoutingTime(Place place1, Place place2, Place place3, int duration2, int duration3) {
		if (place2.getTimeInSeconds() == null) {
			int time = place1.getTimeInSeconds() + duration2;
			if (place3.getTimeInSeconds() != null && place3.getTimeInSeconds().intValue() < time) {
				int x = place3.getTimeInSeconds() - place1.getTimeInSeconds();
				int y = duration2 + duration3;
				int z = x * duration2 / y;
				time = place1.getTimeInSeconds() + z;
			}
			place2.setTimeInSeconds(time);
		}
	}
}
