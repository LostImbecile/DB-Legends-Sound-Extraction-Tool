package processing;

import com.google.gson.JsonObject;

import config.ConfigManager;
import fileops.FileOperations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class EntryAdder {
	private FileOperations fileOperations = new FileOperations();
	private boolean resort;
	private JsonObject jpnResultJson = new JsonObject();
	private JsonObject engResultJson = new JsonObject();
	private JsonObject seResultJson = new JsonObject();
	private JsonObject otherResultJson = new JsonObject();

	List<String[]> links;

	public EntryAdder(String downloadDir, boolean resort, List<String[]> links) {
		if (resort) {
			try {
				fileOperations.indexDirectory(Paths.get(downloadDir));
			} catch (IOException e) {
			}
		}
		this.links = links;
		this.resort = resort;
	}
	
	public void saveFiles() {
		boolean openJPN = ConfigManager.getBooleanProperty("Open_JPN");
		boolean openENG = ConfigManager.getBooleanProperty("Open_ENG");
		boolean openSE = ConfigManager.getBooleanProperty("Open_SE");
		boolean downloadMisc = ConfigManager.getBooleanProperty("Download_Misc");
		boolean generateJson = ConfigManager.getBooleanProperty("Generate_Paths_JSON");
		
		try {
			if (generateJson) {
				if (openJPN)
					try (FileWriter file = new FileWriter("JPN.json")) {
						file.write(jpnResultJson.toString());
						file.flush();
					}
				if (openENG)
					try (FileWriter file = new FileWriter("ENG.json")) {
						file.write(engResultJson.toString());
						file.flush();
					}
				if (openSE)
					try (FileWriter file = new FileWriter("SE.json")) {
						file.write(seResultJson.toString());
						file.flush();
					}
				if (downloadMisc) {
					try (FileWriter file = new FileWriter("Misc.json")) {
						file.write(otherResultJson.toString());
						file.flush();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addEntry(String fileName, String name, JsonObject paths) {
		JsonObject json;
		if (FileType.isJPN(fileName)) {
			json = jpnResultJson;
		} else if (FileType.isEng(fileName)) {
			json = engResultJson;
		} else if (FileType.isSoundEffect(fileName)) {
			json = seResultJson;
		} else {
			json = otherResultJson;
		}
		addEntry(json, name, paths);
	}

	private void addEntry(JsonObject resultJson, String name, JsonObject paths) {
		resultJson.add(name, paths);
		String targetPath = paths.get("target_path").getAsString();
		String url = paths.get("url").getAsString();

		File file = new File(targetPath);
		if (file.exists()) {
			// File already exists, ignore it
			return;
		}

		if (resort) {
			String fileName = paths.get("name").getAsString().split("\\.")[0];
			File targetDir = new File(targetPath);

			if (!fileOperations.moveFilesByKeyword(fileName, targetDir.getParent()))
				links.add(new String[] { url, targetPath });
		} else
			links.add(new String[] { url, targetPath });
	}
}
