package org.jboss.tusk.datastore.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.exception.DataStoreException;

public class FileSystemDataStore implements DataStore {
	
	private static final String DATA_DIR = System.getProperty("tusk.data.dir", "/tmp/tusk");
	private static File DATA_DIR_FILE = null;
	private static final Log LOG = LogFactory.getLog(FileSystemDataStore.class);
	
	public FileSystemDataStore() {
		//make sure that the file system dir exists
		if (DATA_DIR_FILE == null) {
			DATA_DIR_FILE = new File(DATA_DIR);
			if (!DATA_DIR_FILE.exists()) {
				DATA_DIR_FILE.mkdirs();
				LOG.info("Created data directory at " + DATA_DIR);
			}
		}
	}
	
	@Override
	public void put(String id, byte[] data) throws DataStoreException {
		//make sure out parent directory is there
		File parentDir = calculateParentDir(id);
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		
		//now write the file
		File filePath = new File(parentDir, id);
		LOG.info("Writing data for " + id + " to " + filePath);
		try {
			IOUtils.write(data, new FileOutputStream(filePath));
		} catch (FileNotFoundException ex) {
			throw new DataStoreException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DataStoreException(ex.getMessage(), ex);
		}
	}

	@Override
	public byte[] get(String id) throws DataStoreException {
		//figure out file to load and make sure it's there
		File parentDir = calculateParentDir(id);
		File filePath = new File(parentDir, id);
		if (!filePath.exists()) {
			throw new DataStoreException("No file found for id " + id + " at location " + filePath);
		}
		
		//load data from file and return
		LOG.info("Reading data for " + id + " from " + filePath);
		try {
			return FileUtils.readFileToByteArray(filePath);
		} catch (IOException ex) {
			throw new DataStoreException(ex.getMessage(), ex);
		}
	}

	/**
	 * This defines the mapping between a messageKey and the directory in which it lives.
	 * @param messageKey
	 * @return
	 */
	private static File calculateParentDir(String messageKey) {
		return new File(DATA_DIR_FILE, messageKey.substring(0, 4));
	}

}
