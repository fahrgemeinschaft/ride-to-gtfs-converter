package com.ride2go.ridetogtfsconverter.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JSONConverter {

	private static final Logger LOG = LoggerFactory.getLogger(JSONConverter.class);

	@Autowired
	private ObjectMapper objectMapper;

	public String toJSONString(final Object o) {
		if (o != null) {
			try {
				return objectMapper.writeValueAsString(o);
			} catch (JsonProcessingException e) {
				LOG.warn("JSON processing problem while serializing: " + e.getMessage());
			}
		}
		return "";
	}

	public String toPrettyJSONString(final Object o) {
		if (o != null) {
			try {
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
			} catch (JsonProcessingException e) {
				LOG.warn("JSON processing problem while serializing with pretty printer: " + e.getMessage());
			}
		}
		return "";
	}

	public <T> T fromJSONString(String jsonString, Class<T> returnType) {
		if (jsonString != null && returnType != null) {
			try {
				return objectMapper.readValue(jsonString, returnType);
			} catch (JsonMappingException e) {
				LOG.warn("JSON mapping problem while deserializing: " + e.getMessage());
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				LOG.warn("JSON processing problem while deserializing: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
}
