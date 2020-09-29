package com.ride2go.ridetogtfsconverter.model.item.routing.ors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class RouteResponseInfo {

	private String id;

	private String attribution;

	@JsonProperty("osm_file_md5_hash")
	private String osmFileMD5Hash;

	private String service;

	private long timestamp;

	private RouteRequest query;

	private EngineInfo engine;

	@JsonProperty("system_message")
	private String systemMessage;
}
