package com.ride2go.ridetogtfsconverter.model.data.ride;

import static java.time.DayOfWeek.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import com.ride2go.ridetogtfsconverter.conversion.ReoccursDayAttributeConverter;
import lombok.Data;

@Data
@Entity
@Table(name = "fg_trips_reoccurs")
public class EntityReoccurs {

	@Id
	@Column(name = "IDtrip")
	@OrderColumn(name = "ix_trips_reoccurs_IDtrip")
	private String tripId;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean mo;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean tu;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean we;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean th;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean fr;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean sa;

	@Column(columnDefinition = "TINYINT(1)")
	@Convert(converter = ReoccursDayAttributeConverter.class)
	private Boolean su;

	public boolean doesReoccur() {
		return (mo || tu || we || th || fr || sa || su) ? true : false;
	}

	public List<DayOfWeek> getReoccurDays() {
		List<DayOfWeek> reoccurDays = new ArrayList<>();
		if (mo) {
			reoccurDays.add(MONDAY);
		}
		if (tu) {
			reoccurDays.add(TUESDAY);
		}
		if (we) {
			reoccurDays.add(WEDNESDAY);
		}
		if (th) {
			reoccurDays.add(THURSDAY);
		}
		if (fr) {
			reoccurDays.add(FRIDAY);
		}
		if (sa) {
			reoccurDays.add(SATURDAY);
		}
		if (su) {
			reoccurDays.add(SUNDAY);
		}
		return reoccurDays;
	}
}
