package com.ride2go.ridetogtfsconverter.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;

public abstract class RoutingService extends WebClientService {

	private static final Logger LOG = LoggerFactory.getLogger(RoutingService.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public abstract Response calculateRoute(Request request);

	protected void check(final Request request) throws RoutingException {
		if (request == null) {
			throw new RoutingException("request is null");
		}
		if (request.getOrigin() == null) {
			throw new RoutingException("request.origin is null");
		}
		if (request.getDestination() == null) {
			throw new RoutingException("request.destination is null");
		}
	}

	protected static void nullCheck(final Object o, final String message) {
		if (o == null) {
			LOG.warn(message);
		}
	}

	protected static String getAddress(String address, final String message) {
		if (address != null) {
			address = address.trim().replaceFirst("^\\-$", "");
			if (!address.isEmpty()) {
				return address;
			} else {
				LOG.warn(message + "address is empty");
			}
		} else {
			LOG.warn(message + "address is null");
		}
		return null;
	}

	public void changeRelativeToAbsoluteDurations(Response response) {
		// todo
	}

	public void validatePoints(Response response) {
		// todo:
		// null check
		// duplicate check
	}

	protected static String convertToJSON(final Object o) {
		String json = null;
		try {
			json = objectMapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			LOG.warn("JSON processing problem: " + e.getMessage());
		}
		return json;
	}
}
