package com.ride2go.ridetogtfsconverter.conversion;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDateAttributeConverter.class);

	@Override
	public Long convertToDatabaseColumn(final LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		try {
			String localDateString = localDate.format(BASIC_ISO_DATE);
			Long sqlDate = Long.valueOf(localDateString);
			return sqlDate;
		} catch (DateTimeException e) {
			LOG.warn("Entity date '{}' can't be converted. Using null instead.", localDate);
			return null;
		}
	}

	@Override
	public LocalDate convertToEntityAttribute(final Long sqlDate) {
		if (sqlDate == null || sqlDate < 10000000 || sqlDate > 99999999) {
			return null;
		}
		try {
			LocalDate localDate = LocalDate.parse(sqlDate.toString(), BASIC_ISO_DATE);
			return localDate;
		} catch (DateTimeParseException e) {
			LOG.warn("Database date '{}' can't be converted. Using null instead.", sqlDate);
			return null;
		}
	}
}
