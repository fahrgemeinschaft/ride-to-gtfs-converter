package com.ride2go.ridetogtfsconverter.conversion;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.DATA_TIME_FORMATTER;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, Short> {

	private static final Logger LOG = LoggerFactory.getLogger(LocalTimeAttributeConverter.class);

	@Override
	public Short convertToDatabaseColumn(final LocalTime localTime) {
		if (localTime == null) {
			return null;
		}
		try {
			String localDateString = localTime.format(DATA_TIME_FORMATTER);
			Short sqlDate = Short.valueOf(localDateString);
			return sqlDate;
		} catch (DateTimeException e) {
			LOG.warn("Entity time '{}' can't be converted. Using null instead.", localTime);
			return null;
		}
	}

	@Override
	public LocalTime convertToEntityAttribute(final Short sqlTime) {
		if (sqlTime == null || sqlTime < 0 || sqlTime > 2359) {
			return null;
		}
		try {
			String stringTime = sqlTime.toString();
			while (stringTime.length() < 3) {
				stringTime = 0 + stringTime;
			}
			LocalTime localTime = LocalTime.parse(stringTime, DATA_TIME_FORMATTER);
			return localTime;
		} catch (DateTimeParseException e) {
			LOG.warn("Database time '{}' can't be converted. Using null instead.", sqlTime);
			return null;
		}
	}
}
