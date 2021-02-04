package com.ride2go.ridetogtfsconverter.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHandler {

	private static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);

	private FileHandler() {
	}

	public static void zipGtfsDatasetFiles(final File directory, String gtfsZipFile) {
		File[] gtfsTxtFiles = directory.listFiles();
		try {
			FileOutputStream fos = new FileOutputStream(gtfsZipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : gtfsTxtFiles) {
				zos.putNextEntry(new ZipEntry(file.getName()));
				byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
				zos.write(bytes, 0, bytes.length);
				zos.flush();
				zos.closeEntry();
			}
			zos.finish();
			zos.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			LOG.error("Problem packing GTFS zip file {} from all the text files:", gtfsZipFile);
			e.printStackTrace();
		}
	}
}
