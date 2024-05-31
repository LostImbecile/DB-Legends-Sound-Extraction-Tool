package processing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import config.ConfigManager;
import database.DatabaseHandler;
import database.LegendsDatabase;
import objects.Characters;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Processor {
	public static List<String[]> processInputs(String downloadDir) {
		try {
			boolean openJPN = ConfigManager.getBooleanProperty("Open_JPN");
			boolean openENG = ConfigManager.getBooleanProperty("Open_ENG");
			boolean openSE = ConfigManager.getBooleanProperty("Open_SE");
			boolean downloadMisc = ConfigManager.getBooleanProperty("Download_Misc");

			boolean resort = ConfigManager.getBooleanProperty("Resort_Files");

			if (!(openJPN || openENG || openSE || downloadMisc))
				return Collections.emptyList();
			if (openJPN && openENG) {
				System.out.println("Can't download both JP and ENG files at the same time, please disable one of them");
				return Collections.emptyList();
			}

			DatabaseHandler.start();

			System.out.println("Reading input...");

			// Create a map to store character names and their corresponding IDs
			Map<String, String> characterMap = new HashMap<>();
			Map<String, String> idMap = new LinkedHashMap<>();
			FileMapBuilder.buildMaps(characterMap, idMap);

			// Read and parse the urls.json
			String urlsContent = FileUtils.readFileToString(new File("urls.json"), StandardCharsets.UTF_8);
			JsonObject urlsJson = JsonParser.parseString(urlsContent).getAsJsonObject();

			// Create a new JsonObject to store the results
			List<String[]> links = new ArrayList<>();
			EntryAdder entryAdder = new EntryAdder(downloadDir, resort, links);

			System.out.println("Building paths...");
			// Process each URL and build the target path
			for (Map.Entry<String, JsonElement> entry : urlsJson.entrySet()) {
				String path = entry.getKey();
				path = path.substring(path.indexOf("/") + 1);
				if (FileType.isJPN(path)) {
					if (!openJPN)
						continue;
				} else if (FileType.isEng(path)) {
					if (!openENG)
						continue;
				} else if (FileType.isSoundEffect(path)) {
					if (!openSE)
						continue;
				} else if (!downloadMisc)
					continue;

				String targetPath = getTargetPath(characterMap, idMap, path);

				String url = entry.getValue().getAsString();
				targetPath = downloadDir + File.separator + targetPath;

				JsonObject paths = new JsonObject();
				paths.add("name", new JsonPrimitive(path));
				paths.add("url", new JsonPrimitive(url));
				paths.add("target_path", new JsonPrimitive(targetPath));

				entryAdder.addEntry(path, entry.getKey(), paths);
			}

			System.out.println("Writing output...");
			entryAdder.saveFiles();

			return links;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private static String getTargetPath(Map<String, String> characterMap, Map<String, String> idMap, String path) {
		String characterID = CharacterFinder.findCharacterID(characterMap, idMap, path);
		if (characterID != null) {
			boolean isGeneral = false;
			if (characterID.contains("_")) {
				isGeneral = true;
				String[] ids = characterID.split("_");
				for (String i : ids) {
					characterID = i;
					int parsedId = FileType.parseID(i);
					if (parsedId < 1000) {
						break;
					}
				}
			}
			int id = FileType.parseID(characterID);
			Characters character = LegendsDatabase.getCharacterHash().get(id);
			if (character != null)
				return PathBuilder.buildTargetPathKnown(path, character, isGeneral);
			else {
				return PathBuilder.buildTargetPathUnspecified(path);
			}

		} else if (FileType.isVoice(path)) {
			return PathBuilder.buildTargetPathUnspecified(path);
		} else {
			return PathBuilder.buildTargetPathMisc(path);
		}
	}
}
