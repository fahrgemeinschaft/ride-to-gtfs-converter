package com.ride2go.ridetogtfsconverter.gtfs;

import java.util.List;

import com.ride2go.ridetogtfsconverter.model.item.Offer;

public interface WriterService {

	public void writeAsGTFS(final List<Offer> offers, final String directory);
}
