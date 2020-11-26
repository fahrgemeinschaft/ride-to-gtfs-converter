package com.ride2go.ridetogtfsconverter;

import static com.ride2go.ridetogtfsconverter.configuration.SystemConfiguration.AMOUNT_OF_THREADS;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.gtfs.WriterService;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;
import com.ride2go.ridetogtfsconverter.routing.RoutingHandler;
import static com.ride2go.ridetogtfsconverter.validation.Constraints.AREA_BADEN_WUERTTEMBERG;;

@Service
public class RunService {

	private static final Logger LOG = LoggerFactory.getLogger(RunService.class);

	private static final String DEFAULT_GTFS_OUTPUT_DIRECTORY = "data/";

	private static final int PAGE_SIZE = 5000;

	private static final Sort TRIP_SORT = Sort.by(Order.desc("tripId"));

	@Autowired
	private WriterService writerService;

	@Autowired
	private ReaderService readerService;

	@Autowired
	private RoutingHandler routingHandler;

	@Value("${custom.gtfs.trips.area}")
	private String area;

	protected void run(String... args) throws Exception {
		final File directory = getDirectory(args);
		LOG.info("Use directory " + directory);
		final String userId = getUserId(args);
		area();
		writerService.writeProviderInfoAsGTFS(directory);
		if (userId != null) {
			LOG.info("Get all trips from User " + userId);
			processTripsByUser(directory, userId);
		} else {
			LOG.info("UserId not specified. Get trips from all users.");
			processAllTrips(directory);
		}
	}

	private File getDirectory(String... args) throws IOException {
		final String directoryString = getArgAtIndexOrDefault(0, DEFAULT_GTFS_OUTPUT_DIRECTORY, args);
		final File directory = new File(directoryString);
		try {
			if (directory.exists() && directory.isDirectory()) {
				FileUtils.deleteDirectory(directory);
			}
		} catch (IOException e) {
			LOG.error("Problem with given directory");
			throw e;
		}
		return directory;
	}

	private String getUserId(String... args) {
		return getArgAtIndexOrDefault(1, null, args);
	}

	private String getArgAtIndexOrDefault(int i, String defaultValue, String... args) {
		if (args != null && args.length > 0 && args[i] != null) {
			return args[i].trim();
		}
		return defaultValue;
	}

	private void area() {
		if (area.isEmpty()) {
			LOG.info("No area restriction");
		} else {
			if (area.equals(AREA_BADEN_WUERTTEMBERG)) {
				LOG.info("Trips only from the area: " + area);
			} else {
				LOG.info("Area {} not recognized", area);
			}
		}
	}

	private void processTripsByUser(final File directory, String userId) {
		List<Offer> offers = readerService.getOffersByUserId(userId);
		offers = routingHandler.getRoutingInformation(offers);
		writerService.writeOfferDataAsGTFS(offers, directory);
	}

	private void processAllTrips(final File directory)
			throws CancellationException, CompletionException, ExecutionException, InterruptedException {
		boolean stop = false;
		int index = 0;
		final long offerCount = readerService.getOfferByTriptypeAndRelevanceCount();
		while (!stop) {
			CompletableFuture<List<Offer>>[] completableOffers = (CompletableFuture<List<Offer>>[]) new CompletableFuture<?>[AMOUNT_OF_THREADS];
			for (int threadIndex = 0; threadIndex < completableOffers.length; threadIndex++) {
				final Pageable page = PageRequest.of(index + threadIndex, PAGE_SIZE, TRIP_SORT);
				completableOffers[threadIndex] = readerService.getOfferPageAsync(page);
			}
			CompletableFuture.allOf(completableOffers).join();
			for (int threadIndex = 0; threadIndex < completableOffers.length; threadIndex++) {
				List<Offer> offers = routingHandler.getRoutingInformation(completableOffers[threadIndex].get());
				writerService.writeOfferDataAsGTFS(offers, directory);
				if ((index + threadIndex + 1) * PAGE_SIZE >= offerCount) {
					stop = true;
					break;
				}

			}
			index += AMOUNT_OF_THREADS;
		}
	}
}
