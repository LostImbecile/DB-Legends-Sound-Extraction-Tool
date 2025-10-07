package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import objects.CharacterHash;
import objects.Characters;
import objects.Tags;

public class LegendsDatabase {
	private static ArrayList<Characters> charactersList = new ArrayList<>(500);
	private static ArrayList<Tags> tags = new ArrayList<>(100);
	private static CharacterHash characterHash = new CharacterHash();

	public static final String WEBSITE_URL = "https://dblegends.net/characters";
	private static boolean isDataFetchSuccessfull;

	private LegendsDatabase() {
	}

	public static void initialise(String st) {
		Document document = Jsoup.parse(st);
		getData(document);
	}

	public static void initialise() throws IOException {
		Document document = Jsoup.connect(WEBSITE_URL).get();
		getData(document);
	}

	public static List<Characters> getCharactersList() {
		return charactersList;
	}

	public static List<Tags> getTags() {
		return tags;
	}

	public static void getData(Document document) {
		charactersList.clear();
		tags.clear();
		characterHash.clear();

		addSpecialTags();
		getAllTags(document);

		getCharacters(document);

		// 546 is the current unit count as an additional measure
		setDataFetchSuccessfull(charactersList.size() > 545);
	}

	//
	private static void addSpecialTags() {
		/*
		 * Index matters for some of the tags here so don't reorder
		 * the lines, I can use the IDs to add them, but I want to
		 * avoid any future clash without adding more processing steps
		 */
		tags.add(new Tags(12003, "ultra")); // 0
		tags.add(new Tags(12002, "sparking"));// 1
		tags.add(new Tags(12001, "ex"));// 2
		tags.add(new Tags(12000, "hero"));// 3
		tags.add(new Tags(10000, "male"));// 4
		tags.add(new Tags(10001, "female"));// 5
		tags.add(new Tags(15000, "red"));// 6
		tags.add(new Tags(15001, "yel"));// 7
		tags.add(new Tags(15002, "pur"));// 8
		tags.add(new Tags(15003, "grn"));// 9
		tags.add(new Tags(15004, "blu"));// 10
		tags.add(new Tags(15070, "lgt"));// 11
		tags.add(new Tags(-1, "zenkai"));// 12
		tags.add(new Tags(-1, "lf"));// 13
		tags.add(new Tags(-1, "year1"));// 14
		tags.add(new Tags(-1, "year2"));// 15
		tags.add(new Tags(-1, "year3"));// 16
		tags.add(new Tags(-1, "year4"));// 17
		tags.add(new Tags(-1, "year5"));// 18
		tags.add(new Tags(-1, "year6"));// 19
		tags.add(new Tags(-1, "year7"));// 20
		tags.add(new Tags(-1, "year8"));// 21
		tags.add(new Tags(-1, "old"));// 22
		tags.add(new Tags(-1, "new"));// 23
		tags.add(new Tags(-1, "event"));// 24
		tags.add(new Tags(-1, "all")); // 25
		tags.add(new Tags(-1, "assist")); // 26
	}

	private static void getAllTags(Document document) {
		Elements optionElements = document.select("option");

		Pattern removePattern = Pattern.compile("[é()]");

		for (Element option : optionElements) {
			try {
				int value = Integer.parseInt(option.attr("value"));
				String label = option.text().toLowerCase().replace(" ", "_");
				Matcher matcher = removePattern.matcher(label);
				label = matcher.replaceAll("");
				tags.add(new Tags(value, label));
			} catch (NumberFormatException e) {
				// Not an issue here
			}
		}
	}

	private static void getCharacters(Document document) {
        Elements characters = document.select("a.chara-list");
        for (Element character : characters) {
            String charaUrl = character.attr("href");
            String name = character.select(".card-header.name").text();
            String colour = character.select(".backing").attr("class").replaceAll("backing\\s+\\.", "").toUpperCase().strip();
            if (colour.isEmpty()) {
                colour = null;
            }
            String rarity = character.select("div.rarity").attr("class").replace("rarity ", "").toUpperCase();
            if (rarity.isEmpty()) {
                rarity = null;
            }
            String imgUrl = character.select(".character-thumb img").attr("src");

            int siteID = getSiteID(charaUrl);

            Characters newCharacter = new Characters();
            newCharacter.setSiteID(siteID);
            newCharacter.setImageLink(imgUrl);
            newCharacter.setColour(colour);
            newCharacter.setRarity(rarity);
            newCharacter.setCharacterName(processName(name));

			charactersList.add(newCharacter);
			characterHash.put(newCharacter);
		}
	}

	public static int getSiteID(String line) {
		try {
			String st = line.replace("/character/", "");
			return Integer.parseInt(st);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static String processName(String line) {
		try {
			return line.replace("Super Saiyan", "SSJ").replace("SSJ 2", "SSJ2").replace("SSJ 3", "SSJ3")
					.replace("SSJ 4", "SSJ4").replace("SSJ God", "SSG").replace("SSG SS", "SSGSS").strip();
		} catch (StringIndexOutOfBoundsException e) {
			return "Error";
		}
	}

	public static boolean isDataFetchSuccessfull() {
		return isDataFetchSuccessfull;
	}

	public static void setDataFetchSuccessfull(boolean isDataFetchSuccessfull) {
		LegendsDatabase.isDataFetchSuccessfull = isDataFetchSuccessfull;
	}

	public static CharacterHash getCharacterHash() {
		return characterHash;
	}

	public static void setCharacterHash(CharacterHash characterHash) {
		LegendsDatabase.characterHash = characterHash;
	}

	public static void main(String[] args) throws IOException {
		LegendsDatabase.initialise();
	}

}
