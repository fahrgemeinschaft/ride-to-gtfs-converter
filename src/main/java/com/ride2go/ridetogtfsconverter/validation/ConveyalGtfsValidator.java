package com.ride2go.ridetogtfsconverter.validation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.conveyal.gtfs.validator.json.FeedValidationResultSet;
import com.conveyal.gtfs.validator.json.backends.FileSystemFeedBackend;
import com.conveyal.gtfs.validator.json.serialization.JsonSerializer;
import com.ride2go.ridetogtfsconverter.conversion.JSONConverter;
import com.ride2go.ridetogtfsconverter.notification.Alert;

@Service
public class ConveyalGtfsValidator implements GtfsValidator {

	private static final Logger LOG = LoggerFactory.getLogger(ConveyalGtfsValidator.class);

	private static final String ROUTE_TYPE_1700 = "route_type is 1700";

	private static final String DUPLICATE_STOPS = "duplicateStops";

	private static final String MISSING_SHAPE = "MissingShape";

	@Autowired
	private Alert alert;

	@Autowired
	JSONConverter jsonConverter;

	@Value("${custom.mail.recipients:#{null}}")
	public String[] recipients;

	@Override
	public void check(String input, String output) {
		FeedValidationResult result = validateFeed(input);
		printResultToFile(result, output);
		ignoreKnownProblems(result);
		printResultToFile(result, output.replaceAll("\\.(?=(?i)json(?-i)$)", "-critical."));
		notifying(result);
	}

	private FeedValidationResult validateFeed(String input) {
		FileSystemFeedBackend backend = new FileSystemFeedBackend();
		File inputFile = backend.getFeed(input);
		FeedProcessor processor = new FeedProcessor(inputFile);
		FeedValidationResult result = null;
		try {
			processor.run();
			result = processor.getOutput();
		} catch (IOException e) {
			LOG.error("Validation problem running feed processor for {}:", input);
			e.printStackTrace();
		}
		return result;
	}

	private void printResultToFile(FeedValidationResult result, String output) {
		FeedValidationResultSet results = new FeedValidationResultSet();
		results.add(result);
		JsonSerializer serializer = new JsonSerializer(results);
		try {
			serializer.serializeToFile(new File(output));
		} catch (IOException e) {
			LOG.error("Validation problem serializing to output file {}:", output);
			e.printStackTrace();
		}
	}

	private void ignoreKnownProblems(FeedValidationResult result) {
		if (result != null) {
			InvalidValue value;
			Iterator<InvalidValue> routesInvalidValues = getInvalidValues(result.routes);
			if (routesInvalidValues != null) {
				while (routesInvalidValues.hasNext()) {
					value = routesInvalidValues.next();
					if (value != null
							&& value.problemDescription != null
							&& value.problemDescription.equals(ROUTE_TYPE_1700)) {
						routesInvalidValues.remove();
					}
				}
			}
			Iterator<InvalidValue> stopsInvalidValues = getInvalidValues(result.stops);
			if (stopsInvalidValues != null) {
				while (stopsInvalidValues.hasNext()) {
					value = stopsInvalidValues.next();
					if (value != null
							&& value.affectedField != null
							&& value.affectedField.equals(DUPLICATE_STOPS)) {
						stopsInvalidValues.remove();
					}
				}
			}
			ignoreMissingShape(result.stops);
			ignoreMissingShape(result.trips);
			ignoreMissingShape(result.shapes);
		}
	}

	private void notifying(FeedValidationResult result) {
		if (recipients == null || recipients.length == 0) {
			LOG.error("No mail addresses configured to send GTFS feed validation failures to");
		} else if (getAmountOfProblems(result) > 0) {
			alert.send(recipients, "GTFS feed validation failure",
					"Validation result is:\n" + convertResultToString(result));
		}
	}

	private Iterator<InvalidValue> getInvalidValues(ValidationResult result) {
		if (result != null) {
			Set<InvalidValue> invalidValueSet = result.invalidValues;
			if (invalidValueSet != null) {
				return invalidValueSet.iterator();
			}
		}
		return null;
	}

	private void ignoreMissingShape(ValidationResult result) {
		Iterator<InvalidValue> invalidValues = getInvalidValues(result);
		if (invalidValues != null) {
			InvalidValue value;
			while (invalidValues.hasNext()) {
				value = invalidValues.next();
				if (value != null
						&& value.problemType != null
						&& value.problemType.equals(MISSING_SHAPE)) {
					invalidValues.remove();
				}
			}
		}
	}

	private int getAmountOfProblems(FeedValidationResult result) {
		return result == null ? 0
				: getAmountOfProblems(result.routes) + getAmountOfProblems(result.shapes)
						+ getAmountOfProblems(result.stops) + getAmountOfProblems(result.trips);
	}

	private String convertResultToString(FeedValidationResult result) {
		FeedValidationResultSet results = new FeedValidationResultSet();
		results.add(result);
		JsonSerializer serializer = new JsonSerializer(results);
		try {
			String jsonString = (String) serializer.serialize();
			Map<?, ?> map = (Map<?, ?>) jsonConverter.fromJSONString(jsonString, Map.class);
			return jsonConverter.toPrettyJSONString(map);
		} catch (IOException e) {
			LOG.error("Problem while converting result to formated String:");
			e.printStackTrace();
		}
		return null;
	}

	private int getAmountOfProblems(ValidationResult result) {
		if (result != null && result.invalidValues != null) {
			return result.invalidValues.size();
		}
		return 0;
	}
}
