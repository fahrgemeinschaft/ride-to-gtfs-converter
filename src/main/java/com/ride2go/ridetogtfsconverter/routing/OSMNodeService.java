package com.ride2go.ridetogtfsconverter.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.routing.Coordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.osm.Node;
import com.ride2go.ridetogtfsconverter.model.item.routing.osm.Osm;

public class OSMNodeService {

	private static final Logger LOG = LoggerFactory.getLogger(OSMNodeService.class);

	private static final String BASE_URI = "https://api.openstreetmap.org/api/0.6/node/";

	protected static Coordinates convertIdToLatLon(long osmId) {
		Coordinates coordinates = null;
		try {
			String uri = BASE_URI + osmId;
			ClientResponse clientResponse = WebClient.builder()
					.build()
					.get()
					.uri(uri)
					.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
					.exchange()
					.block();
			Osm osm = clientResponse.bodyToMono(Osm.class)
					.block();
			if (osm == null) {
				throw new RoutingException("response body is null");
			}
			Node node = osm.getNode();
			if (node == null) {
				throw new RoutingException("response body element osm.node is null");
			}
			String latitudeString = node.getLat();
			if (latitudeString == null) {
				throw new RoutingException("response body element osm.node.lat is null");
			}
			latitudeString = latitudeString.trim();
			if (latitudeString.isEmpty()) {
				throw new RoutingException("response body element osm.node.lat is empty");
			}
			String longitudeString = node.getLon();
			if (longitudeString == null) {
				throw new RoutingException("response body element osm.node.lon is null");
			}
			longitudeString = longitudeString.trim();
			if (longitudeString.isEmpty()) {
				throw new RoutingException("response body element osm.node.lon is empty");
			}
			double latitude = parseCoordinate(latitudeString);
			double longitude = parseCoordinate(longitudeString);
			coordinates = new Coordinates(latitude, longitude);
		} catch (RoutingException e) {
			LOG.error("OSM error for node {}: {}", osmId, e.getMessage());
		} catch (Exception e) {
			LOG.error("WebClient problem: {}: {}: {}", e.getClass(), e.getCause(), e.getMessage());
		}
		return coordinates;
	}
	
	private static double parseCoordinate(String coordinateString) throws RoutingException {
		try {
			double coordinate = Double.parseDouble(coordinateString);
			return coordinate;
		} catch (NumberFormatException e) {
			String message = String.format("response body element osm.node.lon '%s' can't be parsed to a double: %s",
					coordinateString, e.getMessage());
			throw new RoutingException(message);
		}
	}
}