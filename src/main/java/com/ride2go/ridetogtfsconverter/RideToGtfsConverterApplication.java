package com.ride2go.ridetogtfsconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan("com.ride2go.ridetogtfsconverter")
@EntityScan("com.ride2go.ridetogtfsconverter.model.data.ride")
@EnableJpaRepositories("com.ride2go.ridetogtfsconverter.repository")
@EnableAsync
public class RideToGtfsConverterApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RideToGtfsConverterApplication.class);

	@Autowired
	private RunService runService;

	public static void main(String[] args) {
		SpringApplication.run(RideToGtfsConverterApplication.class, args)
			.close();
		LOG.info("done");
	}

	@Override
	public void run(String... args) throws Exception {
		runService.run(args);
	}
}
