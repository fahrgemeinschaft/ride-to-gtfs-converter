package com.ride2go.ridetogtfsconverter.util;

import java.util.Comparator;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityRouting;

public class EntityRoutingComparator implements Comparator<EntityRouting> {

	@Override
	public int compare(EntityRouting r1, EntityRouting r2) {
		return r1.getIdx().compareTo(r2.getIdx());
	}
}
