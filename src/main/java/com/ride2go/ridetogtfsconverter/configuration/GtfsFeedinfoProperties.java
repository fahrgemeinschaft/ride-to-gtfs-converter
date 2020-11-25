package com.ride2go.ridetogtfsconverter.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties("custom.gtfs.feedinfos")
@Data
public class GtfsFeedinfoProperties {

	private List<Feedinfo> list = new ArrayList<>();

	@Data
	@NoArgsConstructor
	public static class Feedinfo {

		private String publishername;

		private String publisherurl;

		private String lang;

		private String defaultlang;

		private String version;

		private String contactemail;

		private String contacturl;
	}
}
