package com.ride2go.ridetogtfsconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ride2go.ridetogtfsconverter.gtfs.WriterService;
import com.ride2go.ridetogtfsconverter.model.data.ride.EntityUser;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.repository.TripRepository;
import com.ride2go.ridetogtfsconverter.repository.UserRepository;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;

@SpringBootApplication
@ComponentScan({ "com.ride2go.ridetogtfsconverter" })
@EntityScan("com.ride2go.ridetogtfsconverter.model.data.ride")
@EnableJpaRepositories("com.ride2go.ridetogtfsconverter.repository")
@EnableAsync
public class RideToGtfsConverterApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RideToGtfsConverterApplication.class);

	@Autowired
	private ReaderService readerService;

	@Autowired
	private WriterService writerService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TripRepository tripRepository;

	private static final String DEFAULT_GTFS_OUTPUT_DIRECTORY = "data/";

	private static final int AMOUNT_OF_THREADS = 10;

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
			LOG.error("Problem with given directory: " + e.getMessage());
			return;
		}
		writerService.writeProviderInfoAsGTFS(directory);
		if (userId != null) {
			LOG.info("UserId is " + userId);
			processUser(directory, userId);
		} else {
			LOG.info("UserId not specified. Use all.");
			processWithPaging(directory);
			// processByUsers(directory);
		}
	}

	private void processUser(final File directory, final String userId) {
		List<Offer> offersToComplete = readerService.getOffersByUserId(userId);
		writerService.writeOfferDataAsGTFS(offersToComplete, directory);
	}

	private void processWithPaging(final File directory)
			throws CancellationException, CompletionException, ExecutionException, InterruptedException {
		boolean stop = false;
		int i = 0;
		final int pageSize = 5000;
		final long tripCount = tripRepository.count();
		while (!stop) {
			CompletableFuture<List<Offer>>[] completableOffers = (CompletableFuture<List<Offer>>[]) new CompletableFuture<?>[AMOUNT_OF_THREADS];
			for (int j = 0; j < AMOUNT_OF_THREADS; j++) {
				Pageable page = PageRequest.of(i * AMOUNT_OF_THREADS + j, pageSize, Sort.by(Order.asc("tripId")));
				completableOffers[j] = readerService.getOfferPageAsync(page);
			}
			CompletableFuture.allOf(completableOffers).join();
			for (int j = 0; j < completableOffers.length; j++) {
				if ((i * AMOUNT_OF_THREADS + j) * pageSize >= tripCount) {
					stop = true;
				}
				writerService.writeOfferDataAsGTFS(completableOffers[j].get(), directory);
			}
			i++;
		}
	}

	private void processByUsers(final File directory)
			throws CancellationException, CompletionException, ExecutionException, InterruptedException {
		Iterable<EntityUser> users = userRepository.findAll();
		List<EntityUser> userList = new ArrayList<>();
		users.forEach(userList::add);
		int size = userList.size();
		for (int i = 0; i < size; i = i + AMOUNT_OF_THREADS) {
			LOG.info("User " + (i + 1) + " of " + size + " Users");
			CompletableFuture<List<Offer>>[] completableOffers = (CompletableFuture<List<Offer>>[]) new CompletableFuture<?>[AMOUNT_OF_THREADS];
			for (int j = 0; j < AMOUNT_OF_THREADS; j++) {
				completableOffers[j] = readerService.getOffersByUserIdAsync(userList.get(i).getUserId());
			}
			CompletableFuture.allOf(completableOffers).join();
			for (int j = 0; j < completableOffers.length; j++) {
				writerService.writeOfferDataAsGTFS(completableOffers[j].get(), directory);
			}
		}
	}

	private String getDirectory(String... args) {
		String directory = DEFAULT_GTFS_OUTPUT_DIRECTORY;
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
}
