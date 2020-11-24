package com.ride2go.ridetogtfsconverter.routing;

import static com.ride2go.ridetogtfsconverter.routing.OSRMRoutingParameter.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.exception.WebClientException;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.Location;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Annotation;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Geometry;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Intersection;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Leg;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Maneuver;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Measures;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.OSRMResponse;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Route;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Step;

@Service
@Qualifier("OSRM")
public class OSRMRoutingService extends RoutingService {

	private static final Logger LOG = LoggerFactory.getLogger(OSRMRoutingService.class);

	private static final Class<OSRMResponse> RESPONSE_CLASS = OSRMResponse.class;

	private static final OSRMResponse FALLBACK_RESPONSE = new OSRMResponse();

	private static final String MESSAGE = "OSRM response body element ";

	@Autowired
	private OSMNodeService osmNodeService;

	private enum Option {
		ONE, TWO, THREE
	}

	private Option routeShapingPointsOption = Option.THREE;

	public Response calculateRoute(final Request request) {
		Response response = new Response();
		try {
			check(request);
			String uri = getUri(request);
			OSRMResponse osrmResponse = getRequest(uri, RESPONSE_CLASS, FALLBACK_RESPONSE).block();
			if (FALLBACK_RESPONSE.equals(osrmResponse)) {
				return response;
			}
			if (osrmResponse == null) {
				throw new RoutingException("response body is null");
			}
			if (!osrmResponse.getCode().equals("Ok")) {
				throw new RoutingException("response code is " + osrmResponse.getCode());
			}
			if (osrmResponse.getRoutes() == null) {
				throw new RoutingException("response routes are null");
			}
			if (osrmResponse.getRoutes().size() == 0) {
				throw new RoutingException("response routes are empty");
			}
			Route route = osrmResponse.getRoutes().get(0);
			if (route == null) {
				throw new RoutingException("response route is null");
			}
			convert(route, response);
		} catch (WebClientException | RoutingException e) {
			LOG.error("OSRM routing error: " + e.getMessage());
		}
		return response;
	}

	private static String getUri(final Request request) {
		return new StringBuilder()
				.append(BASE_URI)
				.append(request.getOrigin().getLongitude())
				.append(",")
				.append(request.getOrigin().getLatitude())
				.append(";")
				.append(request.getDestination().getLongitude())
				.append(",")
				.append(request.getDestination().getLatitude())
				.append("?steps=")
				.append(STEPS_VALUE)
				.append("&overview=")
				.append(OVERVIEW_VALUE)
				.append("&annotations=")
				.append(ANNOTATIONS_VALUE)
				.append("&geometries=")
				.append(GEOMETRIES_VALUE)
				.toString();
	}

	private void convert(final Route route, Response response) {
		List<Location> routeShapingPoints1 = new ArrayList<>();
		List<Location> routeShapingPoints2 = new ArrayList<>();
		List<Location> routeShapingPoints3 = new ArrayList<>();

		routeShapingPoints1 = getRouteGeometryCoordinates(route);
		List<Leg> legs = route.getLegs();
		if (legs != null) {
			for (Leg leg : legs) {
				if (leg != null) {
					List<Step> steps = leg.getSteps();
					if (steps != null) {
						for (Step step : steps) {
							if (step != null) {
								response.addDrivingInstructionPoint(
										getRouteLegStepManeuverLocation(step));
								response.addStreetIntersectionPoints(
										getRouteLegStepIntersectionLocations(step));
								routeShapingPoints2.addAll(
										getRouteLegStepGeometryCoordinates(step));
							} else {
								LOG.warn(MESSAGE + "route.leg.step is null");
							}
						}
					} else {
						LOG.warn(MESSAGE + "route.leg.steps are null");
					}
					routeShapingPoints3.addAll(
							getRouteLegAnnotationNodesAndConvertOsmIdsToLatLon(leg));
				} else {
					LOG.warn(MESSAGE + "route.leg is null");
				}
			}
		} else {
			LOG.warn(MESSAGE + "route.legs are null");
		}
		switch (routeShapingPointsOption) {
		case ONE:
			response.setRouteShapingPoints(routeShapingPoints1);
			break;
		case TWO:
			response.setRouteShapingPoints(routeShapingPoints2);
			break;
		case THREE:
			addGeoCoordinates(routeShapingPoints3, routeShapingPoints1);
			response.setRouteShapingPoints(routeShapingPoints3);
			break;
		default:
			LOG.warn("No route shaping point list selected.");
			break;
		}

		response.setDistance(
				getDistance(route, MESSAGE + "route."));
		response.setDuration(
				getDuration(route, MESSAGE + "route."));
	}

	private static Location getRouteLegStepManeuverLocation(final Step step) {
		Maneuver maneuver = step.getManeuver();
		boolean geoCoordinatesExist = true;
		List<Double> location = null;
		if (maneuver == null) {
			LOG.warn(MESSAGE + "route.leg.step.maneuver is null");
			geoCoordinatesExist = false;
		} else {
			location = maneuver.getLocation();
			if (location == null) {
				LOG.warn(MESSAGE + "route.leg.step.maneuver.location is null");
				geoCoordinatesExist = false;
			} else {
				if (location.get(0) == null) {
					LOG.warn(MESSAGE + "route.leg.step.maneuver.location longitude is null");
					geoCoordinatesExist = false;
				}
				if (location.get(1) == null) {
					LOG.warn(MESSAGE + "route.leg.step.maneuver.location latitude is null");
					geoCoordinatesExist = false;
				}
			}
		}
		Location point = geoCoordinatesExist ? new Location(location.get(1), location.get(0)) : new Location(null);
		point.setAddress(
				getAddress(step.getName(), MESSAGE + "route.leg.step."));
		point.setDistance(
				getDistance(step, MESSAGE + "route.leg.step."));
		point.setDuration(
				getDuration(step, MESSAGE + "route.leg.step."));
		return point;
	}

	private static List<Location> getRouteLegStepIntersectionLocations(final Step step) {
		List<Location> points = new ArrayList<>();
		List<Intersection> intersections = step.getIntersections();
		if (intersections == null) {
			LOG.warn(MESSAGE + "route.leg.step.intersections are null");
			return points;
		}
		List<Double> location;
		Location point;
		for (Intersection intersection : intersections) {
			if (intersection == null) {
				LOG.warn(MESSAGE + "route.leg.step.intersection is null");
				continue;
			}
			location = intersection.getLocation();
			if (location == null) {
				LOG.warn(MESSAGE + "route.leg.step.intersection.location is null");
				continue;
			}
			if (location.get(0) == null) {
				LOG.warn(MESSAGE + "route.leg.step.intersection.location longitude is null");
				continue;
			}
			if (location.get(1) == null) {
				LOG.warn(MESSAGE + "route.leg.step.intersection.location latitude is null");
				continue;
			}
			point = new Location(location.get(1), location.get(0));
			points.add(point);
		}
		return points;
	}

	private static List<Location> getRouteGeometryCoordinates(final Route route) {
		Geometry geometry = route.getGeometry();
		return getGeometryCoordinates(geometry, MESSAGE + "route.");
	}

	private static List<Location> getRouteLegStepGeometryCoordinates(final Step step) {
		Geometry geometry = step.getGeometry();
		return getGeometryCoordinates(geometry, MESSAGE + "route.leg.step.");
	}

	private static List<Location> getGeometryCoordinates(final Geometry geometry, final String message) {
		List<Location> points = new ArrayList<>();
		if (geometry == null) {
			LOG.warn(message + "geometry is null");
			return points;
		}
		List<List<Double>> coordinates = geometry.getCoordinates();
		if (coordinates == null) {
			LOG.warn(message + "geometry.coordinates are null");
			return points;
		}
		for (List<Double> coordinate : coordinates) {
			if (coordinate == null) {
				LOG.warn(message + "geometry.coordinate is null");
				continue;
			}
			if (coordinate.get(0) == null) {
				LOG.warn(message + "geometry.coordinate longitude is null");
				continue;
			}
			if (coordinate.get(1) == null) {
				LOG.warn(message + "geometry.coordinate latitude is null");
				continue;
			}
			Location point = new Location(coordinate.get(1), coordinate.get(0));
			points.add(point);
		}
		return points;
	}

	private static List<Location> getRouteLegAnnotationNodesAndConvertOsmIdsToLatLon(final Leg leg) {
		Annotation annotation = leg.getAnnotation();
		boolean annotationPointsExist = true;
		List<Long> nodes = null;
		List<Float> distances = null;
		List<Float> durations = null;
		if (annotation == null) {
			LOG.warn(MESSAGE + "route.leg.annotation is null");
			annotationPointsExist = false;
		} else {
			nodes = annotation.getNodes();
			if (nodes == null) {
				LOG.warn(MESSAGE + "route.leg.annotation.nodes are null");
				annotationPointsExist = false;
			} else if (nodes.size() == 0) {
				LOG.warn(MESSAGE + "route.leg.annotation.nodes is an empty list");
				annotationPointsExist = false;
			}
			distances = annotation.getDistance();
			if (distances == null) {
				LOG.warn(MESSAGE + "route.leg.annotation.distances are null");
				annotationPointsExist = false;
			}
			durations = annotation.getDuration();
			if (durations == null) {
				LOG.warn(MESSAGE + "route.leg.annotation.durations are null");
				annotationPointsExist = false;
			}
			if (annotationPointsExist) {
				distances.add(0, 0F);
				durations.add(0, 0F);
				if (nodes.size() != distances.size()
						|| nodes.size() != durations.size()) {
					LOG.warn(MESSAGE + "route.leg.annotation lists have different sizes: {} {} {}",
							nodes.size(), distances.size(), durations.size());
					annotationPointsExist = false;
				}
			}
		}
		if (annotationPointsExist) {
			return getRouteLegAnnotationPoints(nodes, distances, durations);
		} else {
			return Arrays.asList(getRouteLegPoint(leg));
		}
	}

	private static List<Location> getRouteLegAnnotationPoints(final List<Long> nodes, final List<Float> distances,
			final List<Float> durations) {
		List<Location> points = new ArrayList<>();
		Measures measures = new Measures();
		for (int i = 0; i < nodes.size(); i++) {
			nullCheck(nodes.get(i), MESSAGE + "route.leg.annotation.node is null");
			measures.setDistance(distances.get(i));
			measures.setDuration(durations.get(i));

			Location point = new Location(null);
			point.setOsmNodeId(nodes.get(i));
			point.setDistance(
					getDistance(measures, MESSAGE + "route.leg.annotation."));
			point.setDuration(
					getDuration(measures, MESSAGE + "route.leg.annotation."));
			points.add(point);
		}
		return points;
	}

	private static Location getRouteLegPoint(final Leg leg) {
		Location point = new Location(null);
		point.setDistance(
				getDistance(leg, MESSAGE + "route.leg."));
		point.setDuration(
				getDuration(leg, MESSAGE + "route.leg."));
		return point;
	}

	private void addGeoCoordinates(List<Location> routeShapingPoints3, final List<Location> routeShapingPoints1) {
		if (routeShapingPoints3.size() == routeShapingPoints1.size()) {
			for (int i = 0; i < routeShapingPoints3.size(); i++) {
				routeShapingPoints3.get(i).setGeoCoordinates(
						routeShapingPoints1.get(i).getGeoCoordinates());
			}
		} else {
			LOG.warn("Convert OSM node ids to latitude and longitude");
			Long osmNodeId;
			for (Location point : routeShapingPoints3) {
				osmNodeId = point.getOsmNodeId();
				if (osmNodeId != null) {
					GeoCoordinates geoCoordinates = osmNodeService.convertIdToLatLon(osmNodeId);
					if (geoCoordinates != null) {
						point.setGeoCoordinates(geoCoordinates);
					}
				}
			}
		}
	}

	private static Double getDistance(final Measures measures, final String message) {
		if (measures.getDistance() == null) {
			LOG.warn(message + "distance is null");
			return null;
		}
		return Double.valueOf(
				measures.getDistance());
	}

	private static Double getDuration(final Measures measures, final String message) {
		if (measures.getDuration() == null) {
			LOG.warn(message + "duration is null");
			return null;
		}
		return Double.valueOf(
				measures.getDuration());
	}
}
