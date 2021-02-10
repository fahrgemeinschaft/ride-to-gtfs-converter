package com.ride2go.ridetogtfsconverter.validation;

public interface GtfsValidator {

	public String[] getRecipients();

	public void setRecipients(String[] recipients);

	public boolean check(String input, String output);
}
