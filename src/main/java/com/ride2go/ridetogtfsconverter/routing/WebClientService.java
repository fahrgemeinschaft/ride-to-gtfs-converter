package com.ride2go.ridetogtfsconverter.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class WebClientService {

	private static final Logger LOG = LoggerFactory.getLogger(WebClientService.class);

	@Autowired
	private WebClient webClient;

	@Autowired
	private ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

	protected <T> Mono<T> getRequest(String uri, final Class<T> responseClass, final T fallbackResponse) {
		return circuitBreakerFactory.create(uri)
				.run(webClient.get()
						.uri(uri)
						.retrieve()
						.bodyToMono(responseClass).retry(3), throwable -> {
							LOG.error("WebClient problem: {}: {}: {}",
									throwable.getClass(), throwable.getCause(), throwable.getMessage());
							return Mono.just(fallbackResponse);
						});
	}
}
