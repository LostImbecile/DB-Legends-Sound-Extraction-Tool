package main;

import audio.AudioExtractor;
import config.ConfigManager;
import delete.Deleter;
import download.Downloader;
import fileops.FileOperations;
import processing.Processor;
import zip.FolderZipper;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class Main {
	public static void main(String[] args) {

		String downloadDir = getDownloadDir();
		String packagedDir = getPackagedDir();

		download(Processor.processInputs(downloadDir));

		FileOperations.deleteEmptyFoldersRecursively(new File(downloadDir));

		AudioExtractor.extractAndConvert(downloadDir);

		Deleter.deleteFiles(downloadDir);

		FolderZipper.zip(downloadDir, packagedDir,
				ConfigManager.getBooleanProperty("Inlcude_Parent_Folder_In_Package"));
		System.out.println("\nDone!");
	}

	public static String getPackagedDir() {
		String packagedDir = ConfigManager.getProperty("Package_Dir");
		if (packagedDir == null || packagedDir.isBlank())
			packagedDir = "Packaged";
		else {
			try {
				Paths.get(packagedDir);
			} catch (Exception e) {
				packagedDir = "Packaged";
			}
		}
		return packagedDir;
	}

	public static String getDownloadDir() {
		String downloadDir = ConfigManager.getProperty("Download_Dir");

		if (downloadDir == null || downloadDir.isBlank())
			downloadDir = "Downloaded";
		else {
			try {
				Paths.get(downloadDir);
			} catch (Exception e) {
				downloadDir = "Downloaded";
			}
		}
		return downloadDir;
	}

	public static void download(List<String[]> links) {
		boolean dontDownload = ConfigManager.getBooleanProperty("Dont_Download");
		if (!links.isEmpty() && !dontDownload) {
			System.out.println("\nDownloading " + links.size() + " Files");
			try {
				Downloader.openAllLinks(links);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
