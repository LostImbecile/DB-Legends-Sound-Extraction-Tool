package processing;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileMapBuilder {
	public static void buildMaps(Map<String, String> characterMap, Map<String, String> idMap) throws IOException {
		// Read the AssetBundles.json file content into a String
		String assetBundlesContent = FileUtils.readFileToString(new File("AssetBundles.json"), StandardCharsets.UTF_8);
		JsonArray assetBundleNames = JsonParser.parseString(assetBundlesContent).getAsJsonArray();
		for (int i = 0; i < assetBundleNames.size(); i++) {
			String asset = assetBundleNames.get(i).getAsString();
			if (asset.startsWith("character/data/battle/")) {
				String[] parts = asset.split("/");
				String idNameNo = parts[3];
				String[] idNameNoParts = idNameNo.split("_");

				try {
					String id = idNameNoParts[0];
					if (id.equals("9000") || id.equals("9800"))
						id = "1" + id;
					String name = idNameNoParts[1];
					String no = idNameNoParts[2];
					characterMap.putIfAbsent(name + "_" + no, id); // If there's a number
					characterMap.putIfAbsent(name + "_" + id, id);
					idMap.put(id.replaceFirst("^0+(?!$)", ""), name + "_" + no);
				} catch (Exception e) {
				}

			}
		}
	}
}
