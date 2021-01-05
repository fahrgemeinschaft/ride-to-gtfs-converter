package com.ride2go.ridetogtfsconverter.validation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.conveyal.gtfs.model.InvalidValue;
import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.conveyal.gtfs.validator.json.FeedValidationResultSet;
import com.conveyal.gtfs.validator.json.backends.FileSystemFeedBackend;
import com.conveyal.gtfs.validator.json.serialization.JsonSerializer;

@Service
public class ConveyalGtfsValidator implements GtfsValidator {

	private static final Logger LOG = LoggerFactory.getLogger(ConveyalGtfsValidator.class);

	private static final String ROUTE_TYPE_1700 = "route_type is 1700";

	private static final String MISSING_SHAPE = "MissingShape";

	private static final String DUPLICATE_STOPS = "duplicateStops";

	public void check(String input, String output) {
		FileSystemFeedBackend backend = new FileSystemFeedBackend();
		FeedValidationResultSet results = new FeedValidationResultSet();
		File inputFile = backend.getFeed(input);
		FeedProcessor processor = new FeedProcessor(inputFile);
		try {
			processor.run();
			FeedValidationResult result = processor.getOutput();

			ignoreKnownProblems(result);

			results.add(result);
			JsonSerializer serializer = new JsonSerializer(results);
			serializer.serializeToFile(new File(output));
		} catch (IOException e) {
			LOG.error("Validation problem running feed processor for {} or serializing to output file {}: {}", input, output, e.getMessage());
			e.printStackTrace();
		}
	}

	private void ignoreKnownProblems(FeedValidationResult result) {
		ValidationResult routesValidationResult = result.routes;
		Set<InvalidValue> routesInvalidValues = routesValidationResult.invalidValues;
		Iterator<InvalidValue> iterator = routesInvalidValues.iterator();
		InvalidValue value;
		while (iterator.hasNext()) {
			value = iterator.next();
			if (value != null && value.problemDescription != null && value.problemDescription.equals(ROUTE_TYPE_1700)) {
				iterator.remove();
			}
		}

		ValidationResult stopsValidationResult = result.stops;
		Set<InvalidValue> stopsInvalidValues = stopsValidationResult.invalidValues;
		iterator = stopsInvalidValues.iterator();
		while (iterator.hasNext()) {
			value = iterator.next();
			if (value != null && value.problemType != null && value.problemType.equals(MISSING_SHAPE)) {
				iterator.remove();
			} else if (value != null && value.affectedField != null && value.affectedField.equals(DUPLICATE_STOPS)) {
				iterator.remove();
			}
		}

		ValidationResult tripsValidationResult = result.trips;
		Set<InvalidValue> tripsInvalidValues = tripsValidationResult.invalidValues;
		iterator = tripsInvalidValues.iterator();
		while (iterator.hasNext()) {
			value = iterator.next();
			if (value != null && value.problemType != null && value.problemType.equals(MISSING_SHAPE)) {
				iterator.remove();
			}
		}

		ValidationResult shapesValidationResult = result.shapes;
		Set<InvalidValue> shapesInvalidValues = shapesValidationResult.invalidValues;
		iterator = shapesInvalidValues.iterator();
		while (iterator.hasNext()) {
			value = iterator.next();
			if (value != null && value.problemType != null && value.problemType.equals(MISSING_SHAPE)) {
				iterator.remove();
			}
		}
	}
}
