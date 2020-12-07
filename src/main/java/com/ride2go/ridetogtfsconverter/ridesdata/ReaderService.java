package com.ride2go.ridetogtfsconverter.ridesdata;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Pageable;

import com.ride2go.ridetogtfsconverter.model.item.Offer;

public interface ReaderService {

	public List<Offer> getOffersByUserId(String userId);

	public long getOfferByTriptypeAndRelevanceCount();

	public CompletableFuture<List<Offer>> getOfferPageAsync(final Pageable page);
}
