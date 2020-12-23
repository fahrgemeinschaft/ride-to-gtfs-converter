package com.ride2go.ridetogtfsconverter;

import static com.ride2go.ridetogtfsconverter.configuration.SystemConfiguration.AMOUNT_OF_THREADS;
import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TIME_ZONE_ID_BERLIN;
import static com.ride2go.ridetogtfsconverter.validation.Constraints.AREA_BADEN_WUERTTEMBERG;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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

import com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter;
import com.ride2go.ridetogtfsconverter.gtfs.WriterService;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;
import com.ride2go.ridetogtfsconverter.routing.RoutingHandler;
import com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler;

@Service
public class RunService {

	private static final Logger LOG = LoggerFactory.getLogger(RunService.class);

	private static final String DEFAULT_GTFS_OUTPUT_DIRECTORY = "data/";

	private static final String DEFAULT_GTFS_OUTPUT_FILE = "output/gtfs.zip";

	private static final int PAGE_SIZE = 5000;

	private static final Sort TRIP_SORT = Sort.by(Order.desc("tripId"));

	@Autowired
	private WriterService writerService;

	@Autowired
	private ReaderService readerService;

	@Autowired
	private RoutingHandler routingHandler;

	@Value("${custom.gtfs.output.directory}")
	private String gtfsOutputDirectory;

	@Value("${custom.gtfs.output.file}")
	private String gtfsOutputFile;

	@Value("${custom.trips.by-user}")
	private String tripsByUser;

	@Value("${custom.gtfs.trips.area}")
	private String area;

	protected void run() throws Exception {
		DateAndTimeHandler.today = LocalDate.now(TIME_ZONE_ID_BERLIN);
		DateAndTimeHandler.oneMonthFromToday = DateAndTimeHandler.today.plusMonths(1);
		DateAndTimeHandler.oneYearFromToday = DateAndTimeHandler.today.plusYears(1);
		OBAWriterParameter.feedStartDate = DateAndTimeHandler.today;
		OBAWriterParameter.feedEndDate = DateAndTimeHandler.oneMonthFromToday;
		OBAWriterParameter.feedTimePeriodWeekDays = OBAWriterParameter.getFeedTimePeriodWeekDays();

		final File directory = getDirectory();
		LOG.info("Use data directory " + directory);
		final String file = getFile();
		LOG.info("Use output file " + file);
		final String userId = getUserId();
		area();
		writerService.writeProviderInfoAsGTFS(directory);
		if (userId != null) {
			LOG.info("Get all trips from User " + userId);
			processTripsByUser(directory, userId);
		} else {
			LOG.info("UserId not specified. Get trips from all users.");
			processAllTrips(directory);
		}
		writerService.zip(directory, file);
	}

	private File getDirectory() throws IOException {
		final String directoryString = getValueOrDefault(gtfsOutputDirectory, DEFAULT_GTFS_OUTPUT_DIRECTORY);
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

	private String getFile() throws IOException {
		return getValueOrDefault(gtfsOutputFile, DEFAULT_GTFS_OUTPUT_FILE);
	}

	private String getUserId() {
		return getValueOrDefault(tripsByUser, null);
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

	private String getValueOrDefault(String value, String defaultValue) {
		if (value != null) {
			value = value.trim();
			if (!value.isEmpty()) {
				return value;
			}
		}
		return defaultValue;
	}
}
