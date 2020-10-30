package com.ride2go.ridetogtfsconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.ride2go.ridetogtfsconverter.gtfs.WriterService;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityUser;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.repository.UserRepository;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;

@SpringBootApplication
@ComponentScan({ "com.ride2go.ridetogtfsconverter" })
@EntityScan("com.ride2go.ridetogtfsconverter.model.data.ride")
@EnableJpaRepositories("com.ride2go.ridetogtfsconverter.repository")
public class RideToGtfsConverterApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RideToGtfsConverterApplication.class);

	@Autowired
	private ReaderService readerService;

	@Autowired
	private WriterService writerService;

	@Autowired
	private UserRepository userRepository;

	private static final String DEFAULT_DIRECTORY = "data/";

	public static void main(String[] args) {
		SpringApplication.run(RideToGtfsConverterApplication.class, args).close();
		LOG.info("done");
	}

	@Override
	public void run(String... args) throws Exception {
		String directoryString = getDirectory(args);
		String userId = getUserId(args);
		LOG.info("Use directory " + directoryString);
		File directory = new File(directoryString);
		try {
			if (directory.exists() && directory.isDirectory()) {
				FileUtils.deleteDirectory(directory);
			}
		} catch (IOException e) {
			LOG.error("Problem with given directory: {}", e.getMessage());
			return;
		}
		writerService.writeProviderInfoAsGTFS(directory);
		if (userId != null) {
			LOG.info("UserId is " + userId);
			process(directory, userId);
		} else {
			LOG.info("UserId not specified. Use all.");
			Iterable<EntityUser> users = userRepository.findAll();
			List<EntityUser> userList = new ArrayList<>();
			users.forEach(userList::add);
			int count = 1;
			int size = userList.size();
			for (EntityUser user : userList) {
				LOG.info("User " + count++ + " of " + size + " Users");
				process(directory, user.getUserId());
			}
		}
	}

	private void process(File directory, String userId) {
		List<Offer> offers = readerService.getOffersByUserId(userId);
		writerService.writeOfferDataAsGTFS(offers, directory);
	}

	private String getDirectory(String... args) {
		String directory = DEFAULT_DIRECTORY;
		if (args != null && args.length > 0 && args[0] != null) {
			args[0] = args[0].trim();
			if (!args[0].isEmpty()) {
				directory = args[0];
			}
		}
		return directory;
	}

	private String getUserId(String... args) {
		String userId = null;
		if (args != null && args.length > 1 && args[1] != null) {
			args[1] = args[1].trim();
			if (!args[1].isEmpty()) {
				userId = args[1];
			}
		}
		return userId;
	}
}
