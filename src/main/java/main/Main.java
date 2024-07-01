package main;

import audio.AudioExtractor;
import config.ConfigManager;
import delete.Deleter;
import download.Downloader;
import fileops.FileOperations;
import processing.Processor;
import zip.FolderZipper;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
	public static void main(String[] args) {
		String downloadDir = getDirectory("Download_Dir", "Downloaded");
		String packagedDir = getDirectory("Package_Dir", "Packaged");

		Set<String> modifiedDirectories = new HashSet<>();

		modifiedDirectories.addAll(download(Processor.processInputs(downloadDir)));

		FileOperations.deleteEmptyFoldersRecursively(new File(downloadDir));

		modifiedDirectories.addAll(AudioExtractor.extractAndConvert(downloadDir));

		Deleter.deleteFiles(downloadDir);

		modifiedDirectories = getParentFolderNames(modifiedDirectories, downloadDir);

		if (ConfigManager.getBooleanProperty("Zip_Modified_Only")) {
			Set<File> directories = modifiedDirectories.stream().map(File::new).collect(Collectors.toSet());
			FolderZipper.zipDirectories(directories, packagedDir);
		} else {
			FolderZipper.zip(downloadDir, packagedDir);
		}

		writeModifiedDirectories(modifiedDirectories);

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

	public static Set<String> download(List<String[]> links) {
		boolean dontDownload = ConfigManager.getBooleanProperty("Dont_Download");
		if (!links.isEmpty() && !dontDownload) {
			System.out.println("\nDownloading " + links.size() + " Files");
			try {
				return Downloader.openAllLinks(links);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new HashSet<>();
	}

	public static Set<String> getParentFolderNames(Set<String> directories, String downloadDir) {
	    Set<String> secondFolderNames = new HashSet<>();

	    for (String directory : directories) {
	        try {
				int startIndex = directory.lastIndexOf(downloadDir);
				if (startIndex != -1) {
					int folderIndex = startIndex + downloadDir.length() + 1;
				    String remainingPath = directory.substring(folderIndex);
				    int endIndex = remainingPath.indexOf(File.separator);
				    if (endIndex != -1) {
				        String secondFolder = directory.substring(startIndex, folderIndex + endIndex);
				        secondFolderNames.add(secondFolder);
				    }
				}
			} catch (Exception e) {
			}
	    }

	    return secondFolderNames;
	}

	private static void writeModifiedDirectories(Set<String> modifiedDirectories) {
		if (!modifiedDirectories.isEmpty()) {
			try (FileWriter file = new FileWriter("Modified_Directories.txt")) {
				file.write(String.join("\n", modifiedDirectories));
				file.flush();
			} catch (Exception e) {
			}
		}
	}

}
