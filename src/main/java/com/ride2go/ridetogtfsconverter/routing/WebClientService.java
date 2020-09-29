package com.ride2go.ridetogtfsconverter.routing;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.ride2go.ridetogtfsconverter.exception.WebClientException;

public class WebClientService {

	protected static ClientResponse getRequest(final String uri) throws Exception {
		ClientResponse clientResponse = WebClient.builder()
				.build()
				.get()
				.uri(uri)
				.exchange()
				.block();
		if (clientResponse == null) {
			throw new WebClientException("client response is null");
		}
		if (clientResponse.statusCode() != OK) {
			throw new WebClientException("response status code is " + clientResponse.statusCode());
		}
		return clientResponse;
	}
}
