package com.ride2go.ridetogtfsconverter.gtfs;

import java.io.IOException;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBAWriterUtils {

	private static final Logger LOG = LoggerFactory.getLogger(OBAWriterUtils.class);

	private OBAWriterUtils() {
	}

	protected static void close(GtfsRelationalDaoImpl dao) {
		if (dao != null) {
			try {
				dao.close();
			} catch (Exception e) {
				LOG.error("Problem closing GtfsRelationalDaoImpl: " + e.getMessage());
			}
		}
	}

	protected static void close(GtfsReader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.error("Problem closing GtfsReader: " + e.getMessage());
			}
		}
	}

	protected static void close(GtfsWriter writer, String f) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				LOG.error("Could not close {} file:", f);
				e.printStackTrace();
			}
		}
	}
}
