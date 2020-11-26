package com.ride2go.ridetogtfsconverter.routing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.model.item.Place;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RoutingHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RoutingHandler.class);

	@Value("${custom.routing.service}")
	private String routingRervice;

	@Autowired
	@Qualifier("configured")
	private RoutingService routingService;

	private Request routingRequest = new Request();

	private Response routingResponse;

	public List<Offer> getRoutingInformation(List<Offer> offers) {
		boolean remove, intermediatePlacesExist;
		int placesSize = 0;
		int[] durations = null;
		int time;
		for (int i = 0; i < offers.size(); i++) {
			Offer offer = offers.get(i);
			remove = false;
			Place from = offer.getOrigin();
			placesSize = offer.getPlaces().size();
			intermediatePlacesExist = placesSize > 2;
			if (intermediatePlacesExist) {
				durations = new int[placesSize - 1];
				for (int j = 1; j < placesSize - 1; j++) {
					Place to = offer.getPlaces().get(j);
					getRouting(from, to);
					if (routingHasValidDuration()) {
						durations[j - 1] = routingResponse.getDuration().intValue();
					} else if (to.getTimeInSeconds() == null) {
						remove = true;
						break;
					}
					from = to;
				}
			}
			if (!remove) {
				Place to = offer.getDestination();
				getRouting(from, to);
				if (routingHasValidDuration()) {
					if (intermediatePlacesExist) {
						durations[durations.length - 1] = routingResponse.getDuration().intValue();
					} else if (to.getTimeInSeconds() == null) {
						time = from.getTimeInSeconds() + routingResponse.getDuration().intValue();
						to.setTimeInSeconds(
								roundSecondsToClosestFullMinute(time));
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
				setRoutingTimes(offer, durations);
			}
		}
		return offers;
	}

	private void getRouting(final Place from, final Place to) {
		routingRequest.setOrigin(from.getGeoCoordinates());
		routingRequest.setDestination(to.getGeoCoordinates());
		routingResponse = routingService.calculateRoute(routingRequest);
	}

	private boolean routingHasValidDuration() {
		return routingResponse != null
				&& routingResponse.getDuration() != null
				&& routingResponse.getDuration().doubleValue() >= 0;
	}

	private void setRoutingTimes(Offer offer, final int[] durations) {
		Place place1, place2, place3;
		place1 = place2 = null;
		for (int j = 0; j < durations.length - 1; j++) {
			place1 = offer.getPlaces().get(j);
			place2 = offer.getPlaces().get(j + 1);
			place3 = offer.getPlaces().get(j + 2);
			setRoutingTime(place1, place2, place3, durations[j], durations[j + 1]);
			place1 = place2;
			place2 = place3;
		}
		setRoutingTime(place1, place2, new Place(), durations[durations.length - 1], 0);
	}

	private void setRoutingTime(final Place place1, Place place2, final Place place3, int duration1, int duration2) {
		if (place2.getTimeInSeconds() == null) {
			int time = place1.getTimeInSeconds() + duration1;
			if (place3.getTimeInSeconds() != null && place3.getTimeInSeconds().intValue() < time) {
				int x = place3.getTimeInSeconds() - place1.getTimeInSeconds();
				int y = duration1 + duration2;
				int z = x * duration1 / y;
				time = place1.getTimeInSeconds() + z;
			}
			place2.setTimeInSeconds(
					roundSecondsToClosestFullMinute(time));
		}
	}

	private int roundSecondsToClosestFullMinute(int time) {
		return (time + 30) / 60 * 60;
	}
}
