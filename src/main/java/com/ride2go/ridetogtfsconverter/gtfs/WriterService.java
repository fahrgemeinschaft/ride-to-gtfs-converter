package com.ride2go.ridetogtfsconverter.gtfs;

import java.io.File;
import java.util.List;

import com.ride2go.ridetogtfsconverter.model.item.Offer;

public interface WriterService {

	public void writeProviderInfoAsGTFS(final File directory);

	public void writeOfferDataAsGTFS(final List<Offer> offers, final File directory);

	public void zip(final File directory, String gtfsZipFile);
}
