package com.ride2go.ridetogtfsconverter.normalization;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationPropertiesNormalizer {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertiesNormalizer.class);

	public String[] normalizeMailRecipientAddressArray(String[] recipients) {
		if (recipients == null) {
			return new String[0];
		}
		for (int i = 0; i < recipients.length; i++) {
			if (recipients[i] == null || recipients[i].trim().isEmpty()) {
				LOG.error("One mail recipient for GTFS validation alerts is empty and will be removed");
				recipients = ArrayUtils.removeElement(recipients, recipients[i]);
				i--;
			}
		}
		for (String recipient : recipients) {
			recipient = recipient.trim();
		}
		return recipients;
	}
}
