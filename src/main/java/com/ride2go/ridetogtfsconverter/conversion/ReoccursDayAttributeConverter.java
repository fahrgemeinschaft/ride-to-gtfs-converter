package com.ride2go.ridetogtfsconverter.conversion;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ReoccursDayAttributeConverter implements AttributeConverter<Boolean, Byte> {

	@Override
	public Byte convertToDatabaseColumn(final Boolean reoccursDay) {
		return (byte) ((reoccursDay != null && reoccursDay) ? 1 : 0);
	}

	@Override
	public Boolean convertToEntityAttribute(final Byte sqlReoccursDay) {
		return (sqlReoccursDay != null && sqlReoccursDay.intValue() == 1) ? true : false;
	}
}
