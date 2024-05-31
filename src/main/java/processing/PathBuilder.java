package processing;

import java.io.File;

import objects.Characters;

public class PathBuilder {
	private static final String PATH_SEP = File.separator;
	public static String buildTargetPathKnown(String originalPath, Characters character, boolean isGeneral) {
		StringBuilder targetPath = new StringBuilder();

		if (!originalPath.contains("str_vc")) {
			String[] subParts = originalPath.split("_");
			String oldtype = subParts[0].toLowerCase();
			String type = getTypeName(oldtype);
			if (type.equals("sys")) {
				targetPath.append("Messages");
			} else {
				try {
					if (!(character.getBaseName().contains("&")
							|| character.getCharacterName().toLowerCase().contains("assist")))
						targetPath.append(character.getBaseName() + PATH_SEP);
				} catch (Exception e) {
				}

				targetPath.append(processName(character.getCharacterName()));
				targetPath.append(PATH_SEP);
				if (!isGeneral)
					targetPath.append(character.getGameID());
				else
					targetPath.append("General");

				if (!type.equals(oldtype)) {
					targetPath.append(PATH_SEP).append(type);
				} else {
					if (subParts.length > 5 && type.equals("arts")) {

						String category = subParts[3]; // E.g., sp, ultimate, etc.
						category = getCategoryName(category);
						if (category == null) {
							category = getCategoryName(subParts[4].split("\\.")[0]);
						}

						targetPath.append(PATH_SEP).append(category);
					} else if (type.equals("appear")) {
						String name = subParts[2].split("\\.")[0];
						name = getEntranceType(name);

						targetPath.append(PATH_SEP).append(name);
					} else if (type.equals("change")) {
						String name = subParts[2].split("\\.")[0];
						name = getChangeType(name);

						targetPath.append(PATH_SEP).append(name);
					} else if (subParts.length < 5) {
						String name = subParts[2].split("\\.")[0];
						targetPath.append(PATH_SEP).append(name);
					} else {
						targetPath.append(PATH_SEP).append(type);
					}
				}
			}

		} else {
			targetPath.append("Story");
		}
		targetPath.append(PATH_SEP).append(originalPath.split("\\.")[0] + ".acb");

		return targetPath.toString();
	}
	public static String buildTargetPathMisc(String path) {
		StringBuilder targetPath = new StringBuilder();
		if (FileType.isSoundEffect(path)) {
			targetPath.append("Sound Effects").append(PATH_SEP);
			if (path.contains("detailed"))
				targetPath.append("Ultra Cards").append(PATH_SEP);
			targetPath.append(path.split("\\.")[0] + ".acb");
		} else if (path.contains("bgs_")) {
			targetPath.append("Background Sounds");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0] + ".awb");
		} else if (path.contains("bgm_")) {
			targetPath.append("Background Music");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0] + ".awb");
		} else if (path.contains("vfx_")) {
			targetPath.append("VFX");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0]);
		} else if (path.contains("movie")) {
			targetPath.append("Movies");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0]);
		} else if (path.contains("sys_")) {
			targetPath.append("Messages");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0] + ".acb");
		} else {
			targetPath.append("Miscellaneous");
			targetPath.append(PATH_SEP).append(path.split("\\.")[0] + ".acb");
		}

		return targetPath.toString();
	}

	public static String buildTargetPathUnspecified(String originalPath) {
		StringBuilder targetPath = new StringBuilder();
		if (!originalPath.contains("str_vc")) {

			String[] subParts = originalPath.split("_");
			String oldtype = subParts[0].toLowerCase();
			String type = getTypeName(oldtype);
			if (type.equals("sys")) {
				targetPath.append("Messages");
			} else if (!type.equals(oldtype)) {
				targetPath.append("Other").append(PATH_SEP);
				if (subParts.length > 3 && !originalPath.startsWith("str")) {
					String characterName = subParts[2].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(type);
				} else if (subParts.length > 5) {
					String characterName = subParts[3].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(type);
				} else {
					targetPath.append("Miscellaneous");
				}
			} else {
				targetPath.append("Other").append(PATH_SEP);
				if (subParts.length > 5 && type.equals("arts")) {
					String category = subParts[3].toLowerCase(); // E.g., sp, ultimate, etc.
					String characterName = subParts[2].split("\\.")[0];

					category = getCategoryName(category);
					if (category == null) {
						category = getCategoryName(subParts[4].split("\\.")[0]);
					}

					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(category);
					if (originalPath.contains("cooperation"))
						targetPath.append(" (Coop)");
				} else if (type.equals("appear")) {
					String name = subParts[2];
					name = getEntranceType(name);

					String characterName = subParts[3].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(name);
				} else if (type.equals("change")) {
					String name = subParts[2];
					name = getChangeType(name);

					String characterName = subParts[3].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(name);
				} else if (subParts.length > 3 && !originalPath.startsWith("str")) {
					String characterName = subParts[2].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(type);
				} else if (subParts.length > 5) {
					String characterName = subParts[3].split("\\.")[0];
					targetPath.append(characterName);
					targetPath.append(PATH_SEP).append(type);
				} else {
					targetPath.append("Miscellaneous");
				}
			}
		} else {
			targetPath.append("Story");
		}
		targetPath.append(PATH_SEP).append(originalPath.split("\\.")[0] + ".acb");

		return targetPath.toString();
	}

	public static String getChangeType(String name) {
		if (name.equals("special"))
			name = "Special Change";
		else if (name.equals("norm"))
			name = "Normal Change";
		else
			name = "Cover Change";
		return name;
	}

	public static String getEntranceType(String name) {
		if (name.equals("rival"))
			name = "Special Entrance";
		else if (name.equals("norm"))
			name = "Normal Entrance";
		else
			name = "Coop Entrance";
		return name;
	}

	public static String getTypeName(String type) {
		if (type.equals("cha"))
			type = "Battle";
		else if (type.equals("win"))
			type = "Winscreen";
		else if (type.equals("risingrush"))
			type = "Rising Rush";
		else if (type.equals("cutin"))
			type = "Main Ability";
		else if (type.equals("spine") || type.equals("intro"))
			type = "Intro";
		else if (type.contains("unique"))
			type = "Unique Action";
		else if (type.equals("formchange"))
			type = "Transformation";
		else if (type.contains("provo"))
			type = "Provocation";
		else if (type.equals("gacha"))
			type = "Homescreen";
		return type;
	}

	public static String getCategoryName(String category) {
		if (category.equals("ex"))
			category = "Greencard";
		else if (category.equals("ut"))
			category = "Ultimate";
		else if (category.equals("sp"))
			category = "Special Move";
		else if (category.equals("cc"))
			category = "CC";
		else
			category = null;
		return category;
	}
	
	private static String processName(String line) {
		line = line.replace("\\", "");
		line = line.replace("/", "");
		line = line.replace(":", "");
		line = line.replace("*", "");
		line = line.replace("?", "");
		line = line.replace("\"", "");
		line = line.replace("<", "");
		line = line.replace(">", "");
		line = line.replace("|", "");
		return line.strip();
	}
}
