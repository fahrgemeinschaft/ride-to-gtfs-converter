package com.ride2go.ridetogtfsconverter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.ride2go.ridetogtfsconverter.gtfs.OBAWriterService;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;

@SpringBootApplication
@ComponentScan({ "com.ride2go.ridetogtfsconverter" })
@EntityScan("com.ride2go.ridetogtfsconverter.model.data.ride")
@EnableJpaRepositories("com.ride2go.ridetogtfsconverter.repository")
public class RideToGtfsConverterApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RideToGtfsConverterApplication.class);

	@Autowired
	ReaderService readerService;

	@Autowired
	OBAWriterService writerService;

	public static void main(String[] args) {
		SpringApplication.run(RideToGtfsConverterApplication.class, args).close();
		LOG.info("done");
	}

	@Override
	public void run(String... args) throws Exception {
		String userId = "fc35e4a3-3959-8734-e97e-2d4a7577d886";
		String directory = "data/";

		List<Offer> offers = readerService.getOffersByUserId(userId);
		writerService.writeAsGTFS(offers, directory);
	}
}
