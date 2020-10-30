package com.ride2go.ridetogtfsconverter.repository;

import org.springframework.data.repository.CrudRepository;

import com.ride2go.ridetogtfsconverter.model.data.ride.EntityUser;

public interface UserRepository extends CrudRepository<EntityUser, String> {
}
