package database;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fileops.FileUtilities;

public class DatabaseHandler {
	protected static final Logger logger = LogManager.getLogger(DatabaseHandler.class.getName());
	private static final String BACKUP_FILE_NAME = "Website_Backup.txt";

	public static boolean start() {
		System.out.println("Connecting to dblegends.net ...");
		try {
			LegendsDatabase.initialise();
			backupLegendsWebsite();
		} catch (IOException e) {
			try {
				logger.error(e);
				backupLegendsWebsite();
			} catch (IOException e1) {
				LegendsDatabase.getCharacterHash().clear();
				LegendsDatabase.getCharactersList().clear();
				LegendsDatabase.getTags().clear();
			}
		}
		return true;
	}

	private static void backupLegendsWebsite() throws IOException {
		if (LegendsDatabase.isDataFetchSuccessfull()) {
			// Use current website HTML as backup
			System.out.println("Saving backup...");
			saveLegendsWebsiteBackup();
		} else {
			getLegendsWebsiteBackup();
		}
	}

	private static void getLegendsWebsiteBackup() throws IOException {
		if (FileUtilities.isFileExist(BACKUP_FILE_NAME)) {
			System.out.println("Failed to parse website's data correctly. Trying backup...");
			LegendsDatabase
					.initialise(FileUtilities.readInputStream(FileUtilities.getFileInputStream(BACKUP_FILE_NAME, false)));
			if (!LegendsDatabase.isDataFetchSuccessfull()) {
				System.out.println("Backup is missing information.");
			}
		} else {
			System.out.println("There is no backup. Data fetching failed.");
			throw new IOException();
		}
	}

	public static void saveLegendsWebsiteBackup() {
		try {
			FileUtilities.writeToFile(FileUtilities.readURL(LegendsDatabase.WEBSITE_URL), BACKUP_FILE_NAME);
		} catch (Exception e) {
		}
	}
}
