package com.ride2go.ridetogtfsconverter.model.data.ride;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "fg_trips_routing")
public class EntityRouting {

	@Id
	@Column(name = "routingID")
	private String routingId;

	@Column(name = "IDtrip")
	private String tripId;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "IDorigin")
	private EntityRoutingPlace origin;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "IDdestination")
	private EntityRoutingPlace destination;

	@Column(columnDefinition = "TINYINT(1)")
	private Integer idx;
}
