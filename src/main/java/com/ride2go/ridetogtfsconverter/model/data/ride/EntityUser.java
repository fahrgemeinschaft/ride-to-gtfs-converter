package com.ride2go.ridetogtfsconverter.model.data.ride;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sys_user")
public class EntityUser {

	@Id
	@Column(name = "userID")
	private String userId;
}
