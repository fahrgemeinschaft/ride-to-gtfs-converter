package com.ride2go.ridetogtfsconverter.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.exception.OSMException;
import com.ride2go.ridetogtfsconverter.exception.WebClientException;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.osm.Node;
import com.ride2go.ridetogtfsconverter.model.item.routing.osm.Osm;

@Service
public class OSMNodeService extends WebClientService {

	private static final Logger LOG = LoggerFactory.getLogger(OSMNodeService.class);

	private static final String BASE_URI = "https://api.openstreetmap.org/api/0.6/node/";

	private static final Class<Osm> RESPONSE_CLASS = Osm.class;

	private static final Osm FALLBACK_RESPONSE = new Osm();

	protected GeoCoordinates convertIdToLatLon(final long osmId) {
		GeoCoordinates geoCoordinates = null;
		try {
			String uri = BASE_URI + osmId;
			Osm osm = getRequest(uri, RESPONSE_CLASS, FALLBACK_RESPONSE)
					.block();
			if (FALLBACK_RESPONSE.equals(osm)) {
				return geoCoordinates;
			}
			if (osm == null) {
				throw new OSMException("response body is null");
			}
			Node node = osm.getNode();
			if (node == null) {
				throw new OSMException("response body element osm.node is null");
			}
			String latitudeString = node.getLat();
			if (latitudeString == null) {
				throw new OSMException("response body element osm.node.lat is null");
			}
			latitudeString = latitudeString.trim();
			if (latitudeString.isEmpty()) {
				throw new OSMException("response body element osm.node.lat is empty");
			}
			String longitudeString = node.getLon();
			if (longitudeString == null) {
				throw new OSMException("response body element osm.node.lon is null");
			}
			longitudeString = longitudeString.trim();
			if (longitudeString.isEmpty()) {
				throw new OSMException("response body element osm.node.lon is empty");
			}
			double latitude = parseCoordinate(latitudeString);
			double longitude = parseCoordinate(longitudeString);
			geoCoordinates = new GeoCoordinates(latitude, longitude);
		} catch (WebClientException | OSMException e) {
			LOG.error("OSM error for node {}: {}", osmId, e.getMessage());
		}
		return geoCoordinates;
	}

	private double parseCoordinate(final String coordinateString) throws OSMException {
		try {
			double coordinate = Double.parseDouble(coordinateString);
			return coordinate;
		} catch (NumberFormatException e) {
			String message = String.format("response body element osm.node.lon '%s' can't be parsed to a double: %s",
					coordinateString, e.getMessage());
			throw new OSMException(message);
		}
	}
}
