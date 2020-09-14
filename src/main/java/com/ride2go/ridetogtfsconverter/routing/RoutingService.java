package com.ride2go.ridetogtfsconverter.routing;

import static org.springframework.http.HttpStatus.OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;

public abstract class RoutingService {

	private static final Logger LOG = LoggerFactory.getLogger(RoutingService.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public abstract Response calculateRoute(Request request);

	protected void check(Request request) throws RoutingException {
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

	protected ClientResponse getRequest(String uri) throws Exception {
		ClientResponse clientResponse = WebClient.builder()
				.build()
				.get()
				.uri(uri)
				.exchange()
				.block();
		if (clientResponse == null) {
			throw new RoutingException("client response is null");
		}
		if (clientResponse.statusCode() != OK) {
			throw new RoutingException("response status code is " + clientResponse.statusCode());
		}
		return clientResponse;
	}

	protected static void nullCheck(Object o, String message) {
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

	protected static String convertToJSON(Object o) {
		String json = null;
		try {
			json = objectMapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			LOG.warn("JSON processing problem: " + e.getMessage());
		}
		return json;
	}
}
