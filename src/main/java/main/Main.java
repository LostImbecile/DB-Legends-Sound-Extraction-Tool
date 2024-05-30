package main;

import audio.AudioExtractor;
import config.ConfigManager;
import download.Downloader;
import fileops.FileOperations;
import processing.Processor;
import zip.FolderZipper;

import java.io.File;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		String downloadDir = ConfigManager.getProperty("Download_Dir");
		if (downloadDir == null || downloadDir.isBlank()) {
			downloadDir = "Downloaded";
		}

		List<String[]> links = Processor.processInputs(downloadDir);

		boolean dontDownload = ConfigManager.getBooleanProperty("Dont_Download");
		if (!links.isEmpty() && !dontDownload) {
			System.out.println("\nDownloading " + links.size() + " Files");
			try {
				Downloader.openAllLinks(links);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		FileOperations.deleteEmptyFolders(new File(downloadDir));

		AudioExtractor.extractAndConvert(downloadDir);

		FolderZipper.zip(downloadDir, ConfigManager.getBooleanProperty("Inlcude_Parent_Folder_In_Package"));
		System.out.println("\nDone!");
	}

}
