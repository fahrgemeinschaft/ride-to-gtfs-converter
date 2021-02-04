package com.ride2go.ridetogtfsconverter.validation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.onebusaway.csv_entities.exceptions.MissingRequiredEntityException;
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

import lombok.Getter;
import lombok.Setter;

@Service
public class ConveyalGtfsValidator implements GtfsValidator {

	private static final Logger LOG = LoggerFactory.getLogger(ConveyalGtfsValidator.class);

	private static final String MESSAGE_SUBJECT = "GTFS feed validation failure";

	private static final String ROUTE_TYPE_1700 = "route_type is 1700";

	private static final String DUPLICATE_STOPS = "duplicateStops";

	private static final String MISSING_SHAPE = "MissingShape";

	@Autowired
	private Alert alert;

	@Autowired
	JSONConverter jsonConverter;

	@Value("${custom.mail.recipients:#{null}}")
	@Getter
	@Setter
	private String[] recipients;

	@Override
	public void check(String input, String output) {
		if (exists(input)) {
			FeedValidationResult result = validateFeed(input);
			if (result != null) {
				printResultToFile(result, output);
				ignoreKnownProblems(result);
				printResultToFile(result, output.replaceAll("\\.(?=(?i)json(?-i)$)", "-critical."));
				notifying(result);
			}
		}
	}

	private boolean exists(String input) {
		File file = new File(input);
		if (!file.exists()) {
			String message = String.format("GTFS feed zip file %s not found", input);
			LOG.error(message);
			alert.send(recipients, MESSAGE_SUBJECT, message);
			return false;
		}
		return true;
	}

	private FeedValidationResult validateFeed(String input) {
		FileSystemFeedBackend backend = new FileSystemFeedBackend();
		File inputFile = backend.getFeed(input);
		FeedProcessor processor = new FeedProcessor(inputFile);
		FeedValidationResult result = null;
		try {
			processor.run();
			result = processor.getOutput();
			if (result == null) {
				String message = "No GTFS feed validation result";
				LOG.error(message);
				alert.send(recipients, MESSAGE_SUBJECT, message);
			}
		} catch (IOException e) {
			String message = "Validation problem running feed processor for " + input;
			LOG.error(message + ":");
			e.printStackTrace();
			alert.send(recipients, MESSAGE_SUBJECT, message);
		} catch (MissingRequiredEntityException e) {
			String message = "Required entity in GTFS feed not found: " + e.getMessage();
			LOG.error(message);
			alert.send(recipients, MESSAGE_SUBJECT, message);
		} catch (Exception e) {
			String message = "Validation error running feed processor for " + input;
			LOG.error(message + ":");
			e.printStackTrace();
			alert.send(recipients, MESSAGE_SUBJECT, message);
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
			String message = "Validation problem serializing to output file " + output;
			LOG.error(message + ":");
			e.printStackTrace();
			alert.send(recipients, MESSAGE_SUBJECT, message);
		}
	}

	private void ignoreKnownProblems(FeedValidationResult result) {
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

	private void notifying(FeedValidationResult result) {
		if (getAmountOfProblems(result) != 0) {
			String message = "GTFS feed is not valid. Validation result can be found in the json file.";
			if (result.loadFailureReason != null) {
				message += " Reason is: " + result.loadFailureReason;
			}
			LOG.error(message);
			alert.send(recipients, MESSAGE_SUBJECT, "Validation result is:\n" + convertResultToString(result));
		} else {
			LOG.info("GTFS feed is valid");
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
		try {
			return getAmountOfProblems(result.routes)
					+ getAmountOfProblems(result.shapes)
					+ getAmountOfProblems(result.stops)
					+ getAmountOfProblems(result.trips);
		} catch (NoSuchElementException e) {
			return -1;
		}
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
			LOG.error("Problem while converting GTFS feed validation result to formated String:");
			e.printStackTrace();
		}
		return "(Could not be determined)";
	}

	private int getAmountOfProblems(ValidationResult result) throws NoSuchElementException {
		if (result != null && result.invalidValues != null) {
			return result.invalidValues.size();
		}
		throw new NoSuchElementException();
	}
}
