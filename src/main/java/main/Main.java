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
		String downloadDir = getDirectory("Download_Dir", "Downloaded");
		String packagedDir = getDirectory("Package_Dir", "Packaged");

		download(Processor.processInputs(downloadDir));

		FileOperations.deleteEmptyFoldersRecursively(new File(downloadDir));

		AudioExtractor.extractAndConvert(downloadDir);

		Deleter.deleteFiles(downloadDir);

		FolderZipper.zip(downloadDir, packagedDir,
				ConfigManager.getBooleanProperty("Inlcude_Parent_Folder_In_Package"));
		System.out.println("\nDone!");
	}

	private static String getDirectory(String configKey, String defaultName) {
		String dir = ConfigManager.getProperty(configKey);
		if (dir == null || dir.isBlank()) {
			dir = defaultName;
		} else {
			try {
				Paths.get(dir);
			} catch (Exception e) {
				dir = defaultName;
			}
		}
		return dir;
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
