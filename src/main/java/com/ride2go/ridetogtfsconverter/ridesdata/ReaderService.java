package com.ride2go.ridetogtfsconverter.ridesdata;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Pageable;

import com.ride2go.ridetogtfsconverter.model.item.Offer;

public interface ReaderService {

	public List<Offer> getOffersByUserId(final String userId);

	public CompletableFuture<List<Offer>> getOffersByUserIdAsync(final String userId);

	public List<Offer> getOfferPage(final Pageable page);

	public CompletableFuture<List<Offer>> getOfferPageAsync(final Pageable page);
}
