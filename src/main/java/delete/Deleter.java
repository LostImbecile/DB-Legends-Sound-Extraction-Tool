package delete;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import config.ConfigManager;
import fileops.FileOperations;

public class Deleter {
	private static FileOperations inputFolderIndexer = new FileOperations();

	public static void deleteFiles(String inputDir) {
		boolean deleteWAV = ConfigManager.getBooleanProperty("Delete_WAV");
		boolean deleteACB = ConfigManager.getBooleanProperty("Delete_ACB");
		boolean deleteAWB = ConfigManager.getBooleanProperty("Delete_AWB");

		List<String> deleteExt = new ArrayList<>();
		if (deleteWAV)
			deleteExt.add(".wav");
		if (deleteACB)
			deleteExt.add(".acb");
		if (deleteAWB)
			deleteExt.add(".awb");

		if (deleteWAV || deleteACB || deleteAWB) {
			try {
				System.out.println("Indexing for deletion...");
				inputFolderIndexer.indexDirectory(Paths.get(inputDir));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			System.out.println("Deleting files...");
			inputFolderIndexer.deleteFiles(deleteExt);
		}
	}
}
