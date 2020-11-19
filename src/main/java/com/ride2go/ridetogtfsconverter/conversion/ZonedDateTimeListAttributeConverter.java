package com.ride2go.ridetogtfsconverter.conversion;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TIME_ZONE_ID_BERLIN;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class ZonedDateTimeListAttributeConverter implements AttributeConverter<List<ZonedDateTime>, String> {

	private static final Logger LOG = LoggerFactory.getLogger(ZonedDateTimeListAttributeConverter.class);

	@Autowired
	ObjectMapper objectMapper;

	@Override
	public String convertToDatabaseColumn(final List<ZonedDateTime> zonedDateTimeList) {
		if (zonedDateTimeList == null) {
			return "";
		}
		try {
			List<Long> epochSecondList = new ArrayList<>();
			for (ZonedDateTime zonedDateTime : zonedDateTimeList) {
				if (zonedDateTime != null) {
					epochSecondList.add(zonedDateTime.toEpochSecond());
				}
			}
			return epochSecondList.isEmpty() ? "" : objectMapper.writeValueAsString(epochSecondList);
		} catch (JsonProcessingException e) {
			LOG.warn("Entity zoned date time list '{}' can't be converted. Using \"\" instead.", zonedDateTimeList);
			return "";
		}
	}

	@Override
	public List<ZonedDateTime> convertToEntityAttribute(String sqlEpochSecondList) {
		sqlEpochSecondList = sqlEpochSecondList.trim();
		if (sqlEpochSecondList.isEmpty()) {
			return null;
		}
		try {
			long[] epochSecondList = objectMapper.readValue(sqlEpochSecondList, long[].class);
			List<ZonedDateTime> zonedDateTimeList = new ArrayList<>();
			for (long epochSecond : epochSecondList) {
				zonedDateTimeList.add(Instant.ofEpochSecond(epochSecond).atZone(TIME_ZONE_ID_BERLIN));
			}
			return zonedDateTimeList.isEmpty() ? null : zonedDateTimeList;
		} catch (JsonProcessingException e) {
			LOG.warn("Database date epoch second list '{}' can't be converted. Using null instead.",
					sqlEpochSecondList);
			return null;
		}
	}
}
