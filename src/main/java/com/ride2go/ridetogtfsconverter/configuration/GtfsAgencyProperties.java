package com.ride2go.ridetogtfsconverter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("custom.gtfs.agency")
@Data
public class GtfsAgencyProperties {

	private String id;

	private String name;

	private String url;

	private String timezone;

	private String lang;

	private String phone;

	private String fareurl;

	private String email;
}
