package com.ride2go.ridetogtfsconverter;

import static com.ride2go.ridetogtfsconverter.util.DateAndTimeHandler.TIME_ZONE_BERLIN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledService.class);

	@Autowired
	private RunService runService;

	protected boolean enable = false;

	private boolean finished = true;

	@Scheduled(cron = "${custom.scheduling.job.cron}", zone = TIME_ZONE_BERLIN)
	public void run() throws Exception {
		if (enable) {
			if (finished) {
				finished = false;
				runService.run();
				finished = true;
			} else {
				LOG.info("Can't do a new run if the last run is not finished");
			}
		}
	}
}
