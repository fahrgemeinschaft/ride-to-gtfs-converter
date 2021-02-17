package com.ride2go.ridetogtfsconverter.routing;

import static com.ride2go.ridetogtfsconverter.routing.GHRoutingParameter.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.graphhopper.directions.api.client.ApiClient;
import com.graphhopper.directions.api.client.ApiException;
import com.graphhopper.directions.api.client.api.RoutingApi;
import com.graphhopper.directions.api.client.model.ResponseCoordinates;
import com.graphhopper.directions.api.client.model.ResponseInstruction;
import com.graphhopper.directions.api.client.model.RouteResponse;
import com.graphhopper.directions.api.client.model.RouteResponsePath;
import com.ride2go.ridetogtfsconverter.conversion.JSONConverter;
import com.ride2go.ridetogtfsconverter.exception.RoutingException;
import com.ride2go.ridetogtfsconverter.model.item.GeoCoordinates;
import com.ride2go.ridetogtfsconverter.model.item.routing.Location;
import com.ride2go.ridetogtfsconverter.model.item.routing.Request;
import com.ride2go.ridetogtfsconverter.model.item.routing.Response;

@Service
@Qualifier("GH")
public class GHRoutingService extends RoutingService {

	private static final Logger LOG = LoggerFactory.getLogger(GHRoutingService.class);

	private static final String MESSAGE = "GH response element ";

	@Autowired
	JSONConverter jsonConverter;

	@Value("${custom.routing.service.gh.domain:}")
	private String customDomain;

	@Value("${custom.routing.service.gh.key:}")
	private String ghKey;

	// (required) Get your key at graphhopper.com
	private String key;

	public Response calculateRoute(final Request request) {
		Response response = new Response();
		List<String> points = new ArrayList<>();
		ApiClient client = null;
		try {
			check(request);
			String origin = request.getOrigin().getLatitude() + "," + request.getOrigin().getLongitude();
			String destination = request.getDestination().getLatitude() + "," + request.getDestination().getLongitude();
			points = Arrays.asList(origin, destination);
			RoutingApi routing = new RoutingApi();
			if (!customDomain.trim().isEmpty()) {
				client = new ApiClient().setBasePath(customDomain);
				routing = new RoutingApi(client);
			}
			if (key == null) {
				key = System.getProperty("graphhopper.key", ghKey);
			}
			RouteResponse ghResponse = routing.routeGet(points, pointsEncoded, key, locale, instructions, vehicle,
					elevation, calcPoints, pointHint, chDisable, weighting, edgeTraversal, algorithm, heading,
					headingPenalty, passThrough, roundTripDistance, roundTripSeed, alternativeRouteMaxPaths,
					alternativeRouteMaxWeightFactor, alternativeRouteMaxShareFactor);
			if (ghResponse == null) {
				throw new RoutingException("response is null");
			}
			List<RouteResponsePath> paths = ghResponse.getPaths();
			if (paths == null) {
				throw new RoutingException("response paths are null");
			}
			if (paths.size() == 0) {
				throw new RoutingException("response paths are empty");
			}
			RouteResponsePath path = ghResponse.getPaths().get(0);
			if (path == null) {
				throw new RoutingException("first response path is null");
			}
			convert(path, response);
		} catch (RoutingException e) {
			LOG.error("GH routing error: " + e.getMessage());
		} catch (ApiException | IllegalArgumentException e) {
			String messagePart = (client != null) ? "base URL path " + client.getBasePath() + " and " : "";
			LOG.error("GH {} for {}points {}: {}",
					e.getClass().getSimpleName(), messagePart, jsonConverter.toJSONString(points), e.getMessage());
		} catch (Exception e) {
			LOG.error("GH Exception for points {}:", jsonConverter.toJSONString(points));
			e.printStackTrace();
		}
		return response;
	}

	private void convert(final RouteResponsePath path, Response response) {
		response.setRouteShapingPoints(
				getPathPointsCoordinates(path));
		response.setDrivingInstructionPoints(
				getPathInstructions(path, response.getRouteShapingPoints()));

		nullCheck(path.getDistance(), MESSAGE + "path.distance is null");
		response.setDistance(
				path.getDistance());
		if (path.getTime() != null) {
			response.setDuration(
					// milliseconds in seconds
					Double.valueOf(path.getTime()) / 1000);
		} else {
			LOG.warn(MESSAGE + "path.duration is null");
		}
	}

	private static List<Location> getPathPointsCoordinates(final RouteResponsePath path) {
		List<Location> points = new ArrayList<>();
		ResponseCoordinates responseCoordinates = path.getPoints();
		if (responseCoordinates == null) {
			LOG.warn(MESSAGE + "path.points are null");
			return points;
		}
		ArrayList<List> coordinates = responseCoordinates.getCoordinates();
		if (coordinates == null) {
			LOG.warn(MESSAGE + "path.points.coordinates are null");
			return points;
		}
		for (List<?> coordinate : coordinates) {
			points.add(getPathPointsCoordinate(coordinate));
		}
		return points;
	}

	private static Location getPathPointsCoordinate(final List<?> coordinate) {
		Location point = new Location(null);
		if (coordinate == null) {
			LOG.warn(MESSAGE + "path.points.coordinate is null");
			return point;
		}
		if (coordinate.size() != 2) {
			LOG.warn(MESSAGE + "path.points.coordinate does not have 2 elements, it has: " + coordinate.size());
			return point;
		}
		if (coordinate.get(0) == null) {
			LOG.warn(MESSAGE + "path.points.coordinate longitude is null");
			return point;
		}
		if (coordinate.get(1) == null) {
			LOG.warn(MESSAGE + "path.points.coordinate latitude is null");
			return point;
		}
		if (!(coordinate.get(0) instanceof Double)) {
			LOG.warn(MESSAGE + "path.points.coordinate longitude is not a Double");
			return point;
		}
		if (!(coordinate.get(1) instanceof Double)) {
			LOG.warn(MESSAGE + "path.points.coordinate latitude is not a Double");
			return point;
		}
		point = new Location((Double) coordinate.get(1), (Double) coordinate.get(0));
		return point;
	}

	private static List<Location> getPathInstructions(final RouteResponsePath path, final List<Location> routeShapingPoints) {
		List<Location> points = new ArrayList<>();
		List<ResponseInstruction> instructions = path.getInstructions();
		if (instructions == null) {
			LOG.warn(MESSAGE + "path.instructions are null");
			return points;
		}
		for (ResponseInstruction instruction : instructions) {
			if (instruction == null) {
				LOG.warn(MESSAGE + "path.instruction is null");
			} else {
				points.add(getPathInstruction(instruction, routeShapingPoints));
			}
		}
		checkLastInstruction(instructions);
		return points;
	}

	private static Location getPathInstruction(final ResponseInstruction instruction, final List<Location> routeShapingPoints) {
		Location point = new Location(null);
		point.setAddress(
				getAddress(instruction.getStreetName(), MESSAGE + "path.instruction."));
		nullCheck(instruction.getDistance(), MESSAGE + "path.instruction.distance is null");
		point.setDistance(
				instruction.getDistance());
		if (instruction.getTime() == null) {
			LOG.warn(MESSAGE + "path.instruction.duration is null");
		} else {
			point.setDuration(
					Double.valueOf(instruction.getTime()));
		}

		List<Integer> interval = instruction.getInterval();
		if (interval == null) {
			LOG.warn(MESSAGE + "path.instruction.interval is null");
			return point;
		}
		if (interval.size() != 2) {
			LOG.warn(MESSAGE + "path.instruction.interval does not have 2 elements, it has: " + interval.size());
			return point;
		}
		if (interval.get(0) == null) {
			LOG.warn(MESSAGE + "path.instruction.interval first value is null");
			return point;
		}
		if (interval.get(1) == null) {
			LOG.warn(MESSAGE + "path.instruction.interval second value is null");
		}
		if (routeShapingPoints.size() <= interval.get(0)) {
			LOG.warn(
					MESSAGE + "path.instruction.interval first value as an index does not exist in routeShapingPoints");
			return point;
		}
		GeoCoordinates coord = routeShapingPoints.get(interval.get(0)).getGeoCoordinates();
		if (coord == null) {
			LOG.warn(MESSAGE
					+ "path.instruction.interval first value as an index in routeShapingPoints results in null geoCordinates");
			return point;
		}
		GeoCoordinates copy = new GeoCoordinates(coord);
		point.setGeoCoordinates(copy);
		return point;
	}

	private static void checkLastInstruction(final List<ResponseInstruction> instructions) {
		if (instructions.size() > 0) {
			ResponseInstruction lastInstruction = instructions.get(instructions.size() - 1);
			if (lastInstruction != null) {
				List<Integer> lastInterval = lastInstruction.getInterval();
				if (lastInterval != null
						&& lastInterval.size() == 2
						&& lastInterval.get(0) != null
						&& lastInterval.get(1) != null
						&& lastInterval.get(0).intValue() != lastInterval.get(1).intValue()) {
					LOG.warn(
							"GH response body last index element path.instruction.interval has two different values: [{}, {}]",
							lastInterval.get(0), lastInterval.get(1));
				}
			}
		}
	}
}
