package com.ride2go.ridetogtfsconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("com.ride2go.ridetogtfsconverter.configuration")
@ComponentScan("com.ride2go.ridetogtfsconverter")
@EntityScan("com.ride2go.ridetogtfsconverter.model.data.ride")
@EnableJpaRepositories("com.ride2go.ridetogtfsconverter.repository")
@EnableAsync
public class RideToGtfsConverterApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RideToGtfsConverterApplication.class);

	private static final String RUN_ONCE = "runOnce";

	@Autowired
	private RunService runService;

	@Autowired
	private ScheduledService scheduledService;

	private static boolean runOnce;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(RideToGtfsConverterApplication.class, args);
		if (runOnce) {
			context.close();
			LOG.info("Done");
		}
	}

	@Override
	public void run(String... args) throws Exception {
		init(args);
		if (runOnce) {
			LOG.info("Run once");
			runService.run();
		} else {
			LOG.info("Run with schedule");
			scheduledService.enable = true;
		}
	}

	private void init(String... args) {
		if (args != null && args.length > 0 && args[0] != null && args[0].trim().equals(RUN_ONCE)) {
			runOnce = true;
		} else {
			runOnce = false;
		}
	}
}
