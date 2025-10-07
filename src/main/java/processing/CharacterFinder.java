package processing;

import java.util.Iterator;
import java.util.Map;

public class CharacterFinder {
	public static String findCharacterID(Map<String, String> characterMap, Map<String, String> idMap, String path) {
		if (!path.contains(".voice"))
			return null;

		String id = null;
		for (Iterator<String> i = characterMap.keySet().iterator(); i.hasNext();) {
			String key = i.next();

			if (path.contains(key)) {
				id = characterMap.get(key);
				if (!id.startsWith("9") && !id.startsWith("7"))
					return id;
			} else {
				// Check for equal name
				String name = key.substring(0, key.indexOf("_"));
				if (path.contains("_" + name + "_") || path.contains("_" + name + ".")) {
					if (id == null)
						id = characterMap.get(key);
					else
						id = id + "_" + characterMap.get(key); // multiple IDs means it's general
				}
			}
		}

		for (Iterator<String> i = idMap.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			if (key.equals("501"))
				continue;
			if (path.contains("_" + key + "_") || path.contains("_" + key + ".")) {
				// If the ID is 3+ digits, it's a definite match
				if (key.length() >= 3) {
					return key;
				}

				// For shorter IDs, validate against the name in the path
				String characterNameForId = idMap.get(key);
				if (isNameHintInPath(characterNameForId, path)) {
					return key; // ID is validated by name hint.
				}
				// Otherwise, this is likely a false positive (e.g., '24' in a goku file), so we ignore it
			}
		}
		// Check if part of name
		if (id == null) {
			for (Iterator<String> i = characterMap.keySet().iterator(); i.hasNext();) {
				String key = i.next();

				String name = key.substring(0, key.indexOf("_"));
				if (path.contains(name)) {
					if (id == null)
						id = characterMap.get(key);
					else
						id = id + "_" + characterMap.get(key); // multiple IDs means it's general
				}
			}
		}

		return id;
	}

	private static boolean isNameHintInPath(String name, String path) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		String lowerCasePath = path.toLowerCase();
		String[] pathTokens = lowerCasePath.split("[_\\.]");
		
		String lowerCaseName = name.toLowerCase();
		String[] nameTokens = lowerCaseName.split(" ");

		for (String nt : nameTokens) {
			if (nt.length() < 3) continue; // Ignore short tokens
			for (String pt : pathTokens) {
				if (pt.equals(nt)) {
					return true; // Found a matching token
				}
			}
		}

		return false;
	}
}