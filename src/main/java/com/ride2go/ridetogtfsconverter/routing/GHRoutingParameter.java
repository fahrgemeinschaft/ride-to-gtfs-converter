package com.ride2go.ridetogtfsconverter.routing;

import java.util.Arrays;
import java.util.List;

public class GHRoutingParameter {

	private GHRoutingParameter() {
	}

	// (required) IMPORTANT- TODO - currently you have to pass false for the swagger
	// client - Have not found a way to force add a parameter. If `false` the
	// coordinates in `point` and `snapped_waypoints` are returned as array using
	// the order [lon,lat,elevation] for every point. If `true` the coordinates will
	// be encoded as string leading to less bandwith usage. You'll need a special
	// handling for the decoding of this string on the client-side. We provide open
	// source code in
	// [Java](https://github.com/graphhopper/graphhopper/blob/d70b63660ac5200b03c38ba3406b8f93976628a6/web/src/main/java/com/graphhopper/http/WebHelper.java#L43)
	// and
	// [JavaScript](https://github.com/graphhopper/graphhopper/blob/d70b63660ac5200b03c38ba3406b8f93976628a6/web/src/main/webapp/js/ghrequest.js#L139).
	// It is especially important to use no 3rd party client if you set
	// `elevation=true`!
	protected static final Boolean pointsEncoded = false;

	// (optional) The locale of the resulting turn instructions. E.g. `pt_PT` for
	// Portuguese or `de` for German.
	protected static final String locale = "de";

	// (optional) If instruction should be calculated and returned.
	protected static final Boolean instructions = true;

	// (optional) The vehicle for which the route should be calculated. Other
	// vehicles are foot, small_truck, ...
	protected static final String vehicle = "car";

	// (optional) If `true` a third dimension - the elevation - is included in the
	// polyline or in the GeoJson. If enabled you have to use a modified version of
	// the decoding method or set points_encoded to `false`. See the points_encoded
	// attribute for more details. Additionally a request can fail if the vehicle
	// does not support elevation. See the features object for every vehicle.
	protected static final Boolean elevation = false;

	// (optional) If the points for the route should be calculated at all printing
	// out only distance and time.
	protected static final Boolean calcPoints = true;

	// (optional) Optional parameter. Specifies a hint for each `point` parameter to
	// prefer a certain street for the closest location lookup. E.g. if there is an
	// address or house with two or more neighboring streets you can control for
	// which street the closest location is looked up.
	protected static final List<String> pointHint = Arrays.<String>asList();

	// (optional) Use this parameter in combination with one or more parameters of
	// this table.
	protected static final Boolean chDisable = false;

	// (optional) Which kind of 'best' route calculation you need. Other option is
	// `shortest` (e.g. for `vehicle=foot` or `bike`), `short_fastest` if time and
	// distance is expensive e.g. for `vehicle=truck`.
	protected static final String weighting = "fastest";

	// (optional) Use `true` if you want to consider turn restrictions for bike and
	// motor vehicles. Keep in mind that the response time is roughly 2 times
	// slower.
	protected static final Boolean edgeTraversal = null;

	// (optional) The algorithm to calculate the route. Other options are
	// `dijkstra`, `astar`, `astarbi`, `alternative_route` and `round_trip`.
	protected static final String algorithm = null;

	// (optional) Favour a heading direction for a certain point. Specify either one
	// heading for the start point or as many as there are points. In this case
	// headings are associated by their order to the specific points. Headings are
	// given as north based clockwise angle between 0 and 360 degree. This parameter
	// also influences the tour generated with `algorithm=round_trip` and force the
	// initial direction.
	protected static final Integer heading = null;

	// (optional) Penalty for omitting a specified heading. The penalty corresponds
	// to the accepted time delay in seconds in comparison to the route without a
	// heading.
	protected static final Integer headingPenalty = null;

	// (optional) If `true` u-turns are avoided at via-points with regard to the
	// `heading_penalty`.
	protected static final Boolean passThrough = null;

	// (optional) If `algorithm=round_trip` this parameter configures approximative
	// length of the resulting round trip.
	protected static final Integer roundTripDistance = null;

	// (optional) If `algorithm=round_trip` this parameter introduces randomness if
	// e.g. the first try wasn't good.
	protected static final Long roundTripSeed = null;

	// (optional) If `algorithm=alternative_route` this parameter sets the number of
	// maximum paths which should be calculated. Increasing can lead to worse
	// alternatives.
	protected static final Integer alternativeRouteMaxPaths = null;

	// (optional) If `algorithm=alternative_route` this parameter sets the factor by
	// which the alternatives routes can be longer than the optimal route.
	// Increasing can lead to worse alternatives.
	protected static final Integer alternativeRouteMaxWeightFactor = null;

	// (optional) If `algorithm=alternative_route` this parameter specifies how much
	// alternatives routes can have maximum in common with the optimal route.
	// Increasing can lead to worse alternatives.
	protected static final Integer alternativeRouteMaxShareFactor = null;
}
