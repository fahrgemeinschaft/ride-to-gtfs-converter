package com.ride2go.ridetogtfsconverter.routing;

import static com.ride2go.ridetogtfsconverter.routing.OSRMRoutingParameter.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.routing.Coordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.Location;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Annotation;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Geometry;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Intersection;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Leg;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Maneuver;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.OSRMResponse;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Route;
import com.ride2go.ridetogtfsconverter.model.item.routing.osrm.Step;

public class OSRMRoutingService extends RoutingService {

	private static final Logger LOG = LoggerFactory.getLogger(OSRMRoutingService.class);

	private static final String ELEMENT = "OSRM response body element ";

	public Response calculateRoute(Request request) {
		Response response = new Response();
		try {
			check(request);
			String uri = getUri(request);
			ClientResponse clientResponse = getRequest(uri);
			OSRMResponse osrmResponse = clientResponse.bodyToMono(OSRMResponse.class)
					.block();
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
			getRouteGeometryCoordinates(route, response); // 3. point list
			List<Leg> legs = route.getLegs();
			if (legs != null) {
				for (Leg leg : legs) {
					if (leg != null) {
						List<Step> steps = leg.getSteps();
						if (steps != null) {
							for (Step step : steps) {
								if (step != null) {
									getRouteLegStepManeuverLocation(step, response); // 1. point list
									getRouteLegStepIntersectionLocations(step, response); // 2. point list
									// getRouteLegStepGeometryCoordinates(step, response); // 4. point list
								} else {
									LOG.warn(ELEMENT + "route.leg.step is null");
								}
							}
						} else {
							LOG.warn(ELEMENT + "route.leg.steps is null");
						}
						// getRouteLegAnnotationNodesAndConvertOsmIdsToLatLon(leg, response); // 5. point list
					} else {
						LOG.warn(ELEMENT + "route.leg is null");
					}
				}
			} else {
				LOG.warn(ELEMENT + "route.legs is null");
			}
		} catch (NullPointerException e) {
			LOG.error("OSRM routing error! ", e);
		} catch (RoutingException e) {
			LOG.error("OSRM routing error: " + e.getMessage());
		} catch (Exception e) {
			LOG.error("WebClient problem: {}: {}: {}", e.getClass(), e.getCause(), e.getMessage());
		}
		return response;
	}

	private static String getUri(Request request) {
		String uri = new StringBuilder()
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
		return uri;
	}

	private static void getRouteLegStepManeuverLocation(Step step, Response response) {
		Maneuver maneuver = step.getManeuver();
		if (maneuver == null) {
			LOG.warn(ELEMENT + "route.leg.step.maneuver is null");
			return;
		}
		List<Double> location = maneuver.getLocation();
		if (location == null) {
			LOG.warn(ELEMENT + "route.leg.step.maneuver.location is null");
			return;
		}
		if (location.get(1) == null) {
			LOG.warn(ELEMENT + "route.leg.step.maneuver.location.lat is null");
			return;
		}
		if (location.get(0) == null) {
			LOG.warn(ELEMENT + "route.leg.step.maneuver.location.lon is null");
			return;
		}
		Location point = new Location(location.get(1), location.get(0));
		String address = step.getName();
		if (address != null) {
			address = address.trim();
			if (!address.isEmpty()) {
				point.setAddress(step.getName());
			} else {
				LOG.warn(ELEMENT + "route.leg.step.address is empty");
			}
		} else {
			LOG.warn(ELEMENT + "route.leg.step.address is null");
		}
		if (step.getDistance() != null) {
			point.setDistance(step.getDistance());
		} else {
			LOG.warn(ELEMENT + "route.leg.step.distance is null");
		}
		if (step.getDuration() != null) {
			point.setDuration(step.getDuration());
		} else {
			LOG.warn(ELEMENT + "route.leg.step.duration is null");
		}
		response.addInstructionPoint(point);
	}

	private static void getRouteLegStepIntersectionLocations(Step step, Response response) {
		List<Intersection> intersections = step.getIntersections();
		if (intersections == null) {
			LOG.warn(ELEMENT + "route.leg.step.intersections is null");
			return;
		}
		List<Double> location;
		Location point;
		for (Intersection intersection : intersections) {
			if (intersection == null) {
				LOG.warn(ELEMENT + "route.leg.step.intersection is null");
				continue;
			}
			location = intersection.getLocation();
			if (location == null) {
				LOG.warn(ELEMENT + "route.leg.step.intersection.location is null");
				continue;
			}
			if (location.get(1) == null) {
				LOG.warn(ELEMENT + "route.leg.step.intersection.location.lat is null");
				continue;
			}
			if (location.get(0) == null) {
				LOG.warn(ELEMENT + "route.leg.step.intersection.location.lon is null");
				continue;
			}
			point = new Location(location.get(1), location.get(0));
			response.addIntersectionPoint(point);
		}
	}

	private static void getRouteGeometryCoordinates(Route route, Response response) {
		Geometry geometry = route.getGeometry();
		getGeometryCoordinates(geometry, response, "route.");
	}

	private static void getRouteLegStepGeometryCoordinates(Step step, Response response) {
		Geometry geometry = step.getGeometry();
		getGeometryCoordinates(geometry, response, "route.leg.step.");
	}

	private static void getGeometryCoordinates(Geometry geometry, Response response, String elementValuePart) {
		if (geometry == null) {
			LOG.warn(ELEMENT + elementValuePart + "geometry is null");
			return;
		}
		List<List<Double>> coordinates = geometry.getCoordinates();
		if (coordinates == null) {
			LOG.warn(ELEMENT + elementValuePart + "geometry.coordinates is null");
			return;
		}
		Location point;
		for (List<Double> coord : coordinates) {
			if (coord == null) {
				LOG.warn(ELEMENT + elementValuePart + "geometry.coordinate is null");
				continue;
			}
			if (coord.get(1) == null) {
				LOG.warn(ELEMENT + elementValuePart + "geometry.coordinate.lat is null");
				continue;
			}
			if (coord.get(0) == null) {
				LOG.warn(ELEMENT + elementValuePart + "geometry.coordinate.lon is null");
				continue;
			}
			point = new Location(coord.get(1), coord.get(0));
			response.addRouteShapingPoint(point);
		}
	}

	private static void getRouteLegAnnotationNodesAndConvertOsmIdsToLatLon(Leg leg, Response response) {
		Annotation annotation = leg.getAnnotation();
		if (annotation == null) {
			LOG.warn(ELEMENT + "route.leg.annotation is null");
			return;
		}
		List<Long> nodes = annotation.getNodes();
		if (nodes == null) {
			LOG.warn(ELEMENT + "route.leg.annotation.nodes is null");
			return;
		}
		Coordinates coordinates;
		Location point;
		for (Long node : nodes) {
			if (node == null) {
				LOG.warn(ELEMENT + "route.leg.annotation.node is null");
				continue;
			}
			coordinates = OSMNodeService.convertIdToLatLon(node);
			if (coordinates != null) {
				point = new Location(coordinates);
				response.addRouteShapingPoint(point);
			} else {
				LOG.warn(ELEMENT + "route.leg.annotation.node converted to lat and lon is null");
			}
		}
	}
}
