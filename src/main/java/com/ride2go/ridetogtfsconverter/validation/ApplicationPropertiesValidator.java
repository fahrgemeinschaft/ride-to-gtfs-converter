package com.ride2go.ridetogtfsconverter.validation;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationPropertiesValidator {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertiesValidator.class);

	// emailregex.com
	private static final String VALID_EMAIL_ADDRESS = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	@Value("${spring.mail.username:}")
	private String mailSenderAddress;

	public boolean validMailAddresses(String[] recipients) {
		if (mailSenderAddress == null || mailSenderAddress.trim().isEmpty()) {
			LOG.error("Mail sender for GTFS validation alerts must not be empty");
			return false;
		}
		if (!mailSenderAddress.matches(VALID_EMAIL_ADDRESS)) {
			LOG.error("Mail sender for GTFS validation alerts is not a valid e-mail address: " + mailSenderAddress);
			return false;
		}
		if (recipients == null || recipients.length == 0) {
			return false;
		}
		for (String recipient : recipients) {
			if (recipient == null || recipient.trim().isEmpty()) {
				LOG.error("One mail recipient for GTFS validation alerts is empty");
				return false;
			}
			if (!recipient.matches(VALID_EMAIL_ADDRESS)) {
				LOG.error("One mail recipient for GTFS validation alerts is not a valid e-mail address: " + recipient);
				return false;
			}
		}
		return true;
	}

	public boolean validDirectoriesAndFiles(String gtfsDatasetDirectory, String gtfsFile, String gtfsValidationFile) {
		try {
			if (!validGtfsDatasetDirectory(gtfsDatasetDirectory)
					|| !validFile(gtfsFile, "GTFS file", "zip")
					|| !validFile(gtfsValidationFile, "GTFS validation file", "json")) {
				return false;
			}
		} catch (SecurityException e) {
			LOG.error("No read access to file or directory");
			e.printStackTrace();
			return false;
		}
		LOG.info("Use dataset directory: " + gtfsDatasetDirectory);
		LOG.info("Use GTFS file: " + gtfsFile);
		LOG.info("Use GTFS validation file: " + gtfsValidationFile);
		return true;
	}

	private boolean validGtfsDatasetDirectory(String gtfsDatasetDirectory) throws SecurityException {
		if (gtfsDatasetDirectory == null) {
			LOG.error("GTFS dataset directory not configured");
			return false;
		}
		File dir = new File(gtfsDatasetDirectory);
		if (dir.exists() && !dir.isDirectory()) {
			LOG.error("Configured GTFS dataset directory is not a directory");
			return false;
		}
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files != null) {
				boolean success;
				for (File f : files) {
					if (f != null && f.exists() && f.isFile()) {
						success = f.delete();
						if (!success) {
							LOG.error("Previous file {} in the configured GTFS dataset directory can't be deleted",
									f.getPath());
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean validFile(String fileString, String fileName, String expectedExtension) throws SecurityException {
		if (fileString == null) {
			LOG.error(fileName + " not configured");
			return false;
		}
		File file = new File(fileString);
		if (file.exists() && !file.isFile()) {
			LOG.error("Configured {} is not a file", fileName);
			return false;
		}
		try {
			String extension = FilenameUtils.getExtension(fileString);
			if (!extension.toLowerCase().equals(expectedExtension)) {
				LOG.error("Configured {} must be a *.{} file", fileName, expectedExtension);
				return false;
			}
		} catch (IllegalArgumentException e) {
			LOG.error("Configured {} has a corrupt file extension or path name", fileString);
			e.printStackTrace();
			return false;
		}
		if (file.exists()) {
			boolean success = file.delete();
			if (!success) {
				LOG.error("Previous {} {} can't be deleted", fileName, fileString);
				return false;
			}
		}
		File dir = file.getParentFile();
		if (dir == null) {
			LOG.error("Configured {} has no parent directory", fileName);
			return false;
		}
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if (!success) {
				LOG.error("Directory for configured {} can't be created", fileName);
				return false;
			}
		}
		return true;
	}
}
