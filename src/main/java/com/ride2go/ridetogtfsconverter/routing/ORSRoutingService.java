package com.ride2go.ridetogtfsconverter.routing;

import static com.ride2go.ridetogtfsconverter.routing.ORSRoutingParameter.*;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.Location;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.GeoJSONIndividualRouteResponse;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.GeoJSONRouteResponse;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.GeoJSONSummary;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.JSONSegment;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.JSONStep;
import com.ride2go.ridetogtfsconverter.model.item.routing.ors.JSONSummary;

@Service
@Qualifier("ORS")
public class ORSRoutingService extends RoutingService {

	private static final Logger LOG = LoggerFactory.getLogger(ORSRoutingService.class);

	private static final Class<GeoJSONRouteResponse> RESPONSE_CLASS = GeoJSONRouteResponse.class;

	private static final GeoJSONRouteResponse FALLBACK_RESPONSE = new GeoJSONRouteResponse();

	private static final String MESSAGE = "ORS response body element ";

	public Response calculateRoute(final Request request) {
		Response response = new Response();
		try {
			check(request);
			String uri = getUri(request);
			GeoJSONRouteResponse orsResponse = getRequest(uri, RESPONSE_CLASS, FALLBACK_RESPONSE)
					.block();
			if (FALLBACK_RESPONSE.equals(orsResponse)) {
				return response;
			}
			if (orsResponse == null) {
				throw new RoutingException("response body is null");
			}
			List<GeoJSONIndividualRouteResponse> features = orsResponse.getFeatures();
			if (features == null) {
				throw new RoutingException("response features are null");
			}
			if (features.size() == 0) {
				throw new RoutingException("response features are empty");
			}
			GeoJSONIndividualRouteResponse feature = features.get(0);
			if (feature == null) {
				throw new RoutingException("response feature is null");
			}
			convert(feature, response);
		} catch (RoutingException e) {
			LOG.error("ORS routing error: " + e.getMessage());
		}
		return response;
	}

	private static String getUri(final Request request) {
		return new StringBuilder()
				.append(BASE_URI)
				.append("?api_key=")
				.append(API_KEY)
				.append("&start=")
				.append(request.getOrigin().getLongitude())
				.append(",")
				.append(request.getOrigin().getLatitude())
				.append("&end=")
				.append(request.getDestination().getLongitude())
				.append(",")
				.append(request.getDestination().getLatitude())
				.toString();
	}

	private void convert(final GeoJSONIndividualRouteResponse feature, Response response) {
		response.setRouteShapingPoints(
				getFeatureGeometryCoordinates(feature));
		response.setDrivingInstructionPoints(
				getFeaturePropertiesSegmentStepWaypoints(feature, response.getRouteShapingPoints()));

		setResponseDistanceAndDuration(feature, response);
	}

	private static List<Location> getFeatureGeometryCoordinates(final GeoJSONIndividualRouteResponse feature) {
		List<Location> points = new ArrayList<>();
		JSONObject geometry = feature.getGeometry();
		if (geometry == null) {
			LOG.warn(MESSAGE + "feature.geometry is null");
			return points;
		}
		List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
		if (coordinates == null) {
			LOG.warn(MESSAGE + "feature.geometry.coordinates are null");
			return points;
		}
		Location point;
		boolean exists;
		for (List<Double> coordinate : coordinates) {
			exists = true;
			if (coordinate == null) {
				LOG.warn(MESSAGE + "feature.geometry.coordinate is null");
				exists = false;
			} else {
				if (coordinate.size() != 2) {
					LOG.warn(MESSAGE + "feature.geometry.coordinate does not have 2 elements, it has: "
							+ coordinate.size());
					exists = false;
				} else {
					if (coordinate.get(0) == null) {
						LOG.warn(MESSAGE + "feature.geometry.coordinate longitude is null");
						exists = false;
					}
					if (coordinate.get(1) == null) {
						LOG.warn(MESSAGE + "feature.geometry.coordinate latitude is null");
						exists = false;
					}
				}
			}
			point = exists ? new Location(coordinate.get(1), coordinate.get(0)) : new Location(null);
			points.add(point);
		}
		return points;
	}

	private static List<Location> getFeaturePropertiesSegmentStepWaypoints(final GeoJSONIndividualRouteResponse feature,
			final List<Location> routeShapingPoints) {
		List<Location> points = new ArrayList<>();
		GeoJSONSummary properties = feature.getProperties();
		if (properties == null) {
			LOG.warn(MESSAGE + "feature.properties is null");
			return points;
		}
		List<JSONSegment> segments = properties.getSegments();
		if (segments == null) {
			LOG.warn(MESSAGE + "feature.properties.segments are null");
			return points;
		}
		for (JSONSegment segment : segments) {
			if (segment == null) {
				LOG.warn(MESSAGE + "feature.properties.segment is null");
				continue;
			}
			List<JSONStep> steps = segment.getSteps();
			if (steps == null) {
				LOG.warn(MESSAGE + "feature.properties.segment.steps are null");
				continue;
			}
			for (JSONStep step : steps) {
				if (step == null) {
					LOG.warn(MESSAGE + "feature.properties.segment.step is null");
				} else {
					points.add(getFeaturePropertiesSegmentStepWaypoint(step, routeShapingPoints));
				}
			}
			checkLastStep(steps);
		}
		return points;
	}

	private static Location getFeaturePropertiesSegmentStepWaypoint(final JSONStep step,
			final List<Location> routeShapingPoints) {
		Location point = new Location(null);
		point.setAddress(
				getAddress(step.getName(), MESSAGE + "feature.properties.segment.step."));
		nullCheck(step.getDistance(), MESSAGE + "feature.properties.segment.step.distance is null");
		nullCheck(step.getDuration(), MESSAGE + "feature.properties.segment.step.duration is null");
		point.setDistance(
				step.getDistance());
		point.setDuration(
				step.getDuration());

		Integer[] wayPoints = step.getWayPoints();
		if (wayPoints == null) {
			LOG.warn(MESSAGE + "feature.properties.segment.step.wayPoints is null");
			return point;
		}
		if (wayPoints.length != 2) {
			LOG.warn(MESSAGE + "feature.properties.segment.step.wayPoints does not have 2 elements, it has: "
					+ wayPoints.length);
			return point;
		}
		if (wayPoints[0] == null) {
			LOG.warn(MESSAGE + "feature.properties.segment.step.wayPoints first value is null");
			return point;
		}
		nullCheck(wayPoints[1], MESSAGE + "feature.properties.segment.step.wayPoints second value is null");
		int index = wayPoints[0].intValue();
		if (routeShapingPoints.size() <= index) {
			LOG.warn(MESSAGE
					+ "feature.properties.segment.step.wayPoints first value as an index does not exist in routeShapingPoints");
			return point;
		}
		if (routeShapingPoints.get(index).getGeoCoordinates() == null) {
			LOG.warn(MESSAGE
					+ "feature.properties.segment.step.wayPoints first value as an index in routeShapingPoints results in null geoCordinates");
			return point;
		}
		GeoCoordinates copy = new GeoCoordinates(
				routeShapingPoints.get(index).getGeoCoordinates());
		point.setGeoCoordinates(copy);
		return point;
	}

	private static void checkLastStep(final List<JSONStep> steps) {
		if (steps.size() > 0) {
			JSONStep lastStep = steps.get(steps.size() - 1);
			if (lastStep != null) {
				Integer[] lastWayPoints = lastStep.getWayPoints();
				if (lastWayPoints != null
						&& lastWayPoints.length == 2
						&& lastWayPoints[0] != null
						&& lastWayPoints[1] != null
						&& lastWayPoints[0].intValue() != lastWayPoints[1].intValue()) {
					LOG.warn(
							"ORS response body last index element feature.properties.segment.step.wayPoints has two different values: [{}, {}]",
							lastWayPoints[0], lastWayPoints[1]);
				}
			}
		}
	}

	private static void setResponseDistanceAndDuration(final GeoJSONIndividualRouteResponse feature,
			Response response) {
		GeoJSONSummary properties = feature.getProperties();
		if (properties != null) {
			JSONSummary summary = properties.getSummary();
			if (summary != null) {
				nullCheck(summary.getDistance(), MESSAGE + "feature.properties.summary.distance is null");
				nullCheck(summary.getDuration(), MESSAGE + "feature.properties.summary.duration is null");
				response.setDistance(
						summary.getDistance());
				response.setDuration(
						summary.getDuration());
			} else {
				LOG.warn(MESSAGE + "feature.properties.summary is null");
			}
		}
	}
}
