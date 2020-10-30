package com.ride2go.ridetogtfsconverter.model.data.ride;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.ride2go.ridetogtfsconverter.conversion.LocalDateAttributeConverter;
import com.ride2go.ridetogtfsconverter.conversion.LocalTimeAttributeConverter;

import lombok.Data;

@Data
@Entity
@Table(name = "fg_trips")
public class EntityTrip {

	@Id
	@Column(name = "tripID")
	private String tripId;

	@Column(name = "IDuser")
	private String userId;

	@Column(columnDefinition = "ENUM('offer', 'search', '')")
	private String triptype;

	@Column(columnDefinition = "INT(11)")
	@Convert(converter = LocalDateAttributeConverter.class)
	private LocalDate startdate;

	@Column(columnDefinition = "SMALLINT(6)")
	@Convert(converter = LocalTimeAttributeConverter.class)
	private LocalTime starttime;

	@Column(columnDefinition = "TINYINT(1)")
	private Integer relevance;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "fg_trips_routing",
		joinColumns = @JoinColumn(name = "IDtrip"),
		inverseJoinColumns = @JoinColumn(name = "routingID"))
	private List<EntityRouting> routings;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "fg_trips_reoccurs",
		joinColumns = @JoinColumn(name = "IDtrip"),
		inverseJoinColumns = @JoinColumn(name = "tripID"))
	private EntityReoccurs reoccurs;
}
