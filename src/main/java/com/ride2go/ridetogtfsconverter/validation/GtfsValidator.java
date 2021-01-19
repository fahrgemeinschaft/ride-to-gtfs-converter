package com.ride2go.ridetogtfsconverter.validation;

public interface GtfsValidator {

	public String[] getRecipients();
	
	public void check(String input, String output);
}
