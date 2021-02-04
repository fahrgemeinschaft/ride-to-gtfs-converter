package com.ride2go.ridetogtfsconverter.configuration;

import java.time.Duration;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.ride2go.ridetogtfsconverter.routing.GHRoutingService;
import com.ride2go.ridetogtfsconverter.routing.ORSRoutingService;
import com.ride2go.ridetogtfsconverter.routing.OSRMRoutingService;
import com.ride2go.ridetogtfsconverter.routing.RoutingService;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class SystemConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

	public static final int AMOUNT_OF_THREADS = 10;

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(AMOUNT_OF_THREADS);
		executor.setMaxPoolSize(AMOUNT_OF_THREADS);
		executor.setQueueCapacity(AMOUNT_OF_THREADS);
		executor.setThreadNamePrefix("dbLookup-");
		executor.initialize();
		return executor;
	}

	@Bean
	public WebClient getWebClientBuilder() {
		return WebClient.builder()
				.exchangeStrategies(
						ExchangeStrategies.builder()
							.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
							.build()
				)
				.build();
	}

	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
		TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
				.timeoutDuration(Duration.ofSeconds(10))
				.build();
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(circuitBreakerConfig)
				.timeLimiterConfig(timeLimiterConfig)
				.build());
	}

	@Bean
	@Qualifier("configured")
	public RoutingService getRoutingService(@Value("${custom.routing.service:}") final String routingServiceChoice) {
		if (routingServiceChoice.equals("GH")) {
			LOG.info("Use GraphHopper as routing engine");
			return new GHRoutingService();
		}
		if (routingServiceChoice.equals("ORS")) {
			LOG.info("Use openrouteservice as routing engine");
			return new ORSRoutingService();
		}
		if (routingServiceChoice.equals("OSRM")) {
			LOG.info("Use OSRM as routing engine");
			return new OSRMRoutingService();
		}
		LOG.info("Routing engine not specified, use OSRM as default routing engine");
		return new OSRMRoutingService();
	}
}
