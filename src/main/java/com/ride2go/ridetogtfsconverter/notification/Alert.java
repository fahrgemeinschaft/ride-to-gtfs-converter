package com.ride2go.ridetogtfsconverter.notification;

public interface Alert {

	public void send(String[] recipients, String subject, String text);
}
