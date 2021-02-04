package com.ride2go.ridetogtfsconverter;

import static com.ride2go.ridetogtfsconverter.configuration.SystemConfiguration.AMOUNT_OF_THREADS;
import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TIME_ZONE_ID_BERLIN;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.ride2go.ridetogtfsconverter.exception.OBAException;
import com.ride2go.ridetogtfsconverter.gtfs.OBAWriterParameter;
import com.ride2go.ridetogtfsconverter.gtfs.WriterService;
import com.ride2go.ridetogtfsconverter.model.item.Offer;
import com.ride2go.ridetogtfsconverter.normalization.ApplicationPropertiesNormalizer;
import com.ride2go.ridetogtfsconverter.ridesdata.ReaderService;
import com.ride2go.ridetogtfsconverter.routing.RoutingHandler;
import com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler;
import com.ride2go.ridetogtfsconverter.util.FileHandler;
import com.ride2go.ridetogtfsconverter.validation.ApplicationPropertiesValidator;
import com.ride2go.ridetogtfsconverter.validation.GtfsValidator;

@Service
public class RunService {

	private static final Logger LOG = LoggerFactory.getLogger(RunService.class);

	private static final int PAGE_SIZE = 5000;

	private static final Sort TRIP_SORT = Sort.by(Order.desc("tripId"));

	@Autowired
	private ApplicationPropertiesNormalizer applicationPropertiesNormalizer;

	@Autowired
	private ApplicationPropertiesValidator applicationPropertiesValidator;

	@Autowired
	private ReaderService readerService;

	@Autowired
	private RoutingHandler routingHandler;

	@Autowired
	private WriterService writerService;

	@Autowired
	private GtfsValidator gtfsValidator;

	@Value("${custom.gtfs.dataset.directory:data/dataset/}")
	private String gtfsDatasetDirectory;

	@Value("${custom.gtfs.file:data/output/gtfs.zip}")
	private String gtfsFile;

	@Value("${custom.gtfs.validation.file:data/validation/results.json}")
	private String gtfsValidationFile;

	@Value("${custom.trips.by-user:#{null}}")
	private String tripsByUser;

	protected void run() throws Exception {
		updateDateAndTimeParameter();
		if (applicationPropertiesValidator.validDirectoriesAndFiles(
				gtfsDatasetDirectory, gtfsFile, gtfsValidationFile)) {
			applicationPropertiesValidator.validArea();
			final File directory = new File(gtfsDatasetDirectory);
			try {
				writerService.writeProviderInfoAsGTFS(directory);
				if (tripsByUser != null && !tripsByUser.trim().isEmpty()) {
					LOG.info("Get all trips from User " + tripsByUser);
					processTripsByUser(directory, tripsByUser);
				} else {
					LOG.info("UserId not specified. Get trips from all users.");
					processAllTrips(directory);
				}
			} catch (OBAException e) {
				LOG.error("Problem while writing GTFS data with OneBusAway: " + e.getMessage());
			}
			FileHandler.zipGtfsDatasetFiles(directory, gtfsFile);
			gtfsValidator.setRecipients(
					applicationPropertiesNormalizer.normalizeMailRecipientAddressArray(
							gtfsValidator.getRecipients()));
			applicationPropertiesValidator.validMailAddresses(
					gtfsValidator.getRecipients());
			gtfsValidator.check(gtfsFile, gtfsValidationFile);
		}
	}

	private void updateDateAndTimeParameter() {
		DateAndTimeHandler.today = LocalDate.now(TIME_ZONE_ID_BERLIN);
		DateAndTimeHandler.oneMonthFromToday = DateAndTimeHandler.today.plusMonths(1);
		DateAndTimeHandler.oneYearFromToday = DateAndTimeHandler.today.plusYears(1);
		OBAWriterParameter.feedStartDate = DateAndTimeHandler.today;
		OBAWriterParameter.feedEndDate = DateAndTimeHandler.oneMonthFromToday;
		OBAWriterParameter.feedTimePeriodWeekDays = OBAWriterParameter.getFeedTimePeriodWeekDays();
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
