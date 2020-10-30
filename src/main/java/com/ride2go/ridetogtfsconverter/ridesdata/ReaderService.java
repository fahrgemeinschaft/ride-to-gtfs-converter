package com.ride2go.ridetogtfsconverter.ridesdata;

import java.util.List;

import com.ride2go.ridetogtfsconverter.model.item.Offer;

public interface ReaderService {

	public List<Offer> getOffersByUserId(final String userId);
}
