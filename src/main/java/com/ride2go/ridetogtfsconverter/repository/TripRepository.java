package com.ride2go.ridetogtfsconverter.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityTrip;

public interface TripRepository extends CrudRepository<EntityTrip, String> {

	long countByTriptypeAndRelevance(String triptype, Integer relevance);

	List<EntityTrip> findByUserIdAndTriptypeAndRelevance(String userId, String triptype, Integer relevance);

	List<EntityTrip> findByTriptypeAndRelevance(String triptype, Integer relevance, Pageable pageable);
}
