package objects;

import java.awt.Color;

public class Characters {
	private String characterName;
	private String baseName = null;
	private String rarity;
	private String colour;
	private String gameID;
	private String imageLink;
	private int siteID;
	private boolean isZenkai;
	private boolean isLF;

	public Characters() {
		// Rarely needed
	}

	public boolean isLF() {
		return isLF;
	}

	public void setLF(boolean isLF) {
		this.isLF = isLF;
	}

	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}
	
	public boolean isUltra() {
		return this.rarity.equalsIgnoreCase("ULTRA");
	}
	
	public boolean isSparking() {
		return this.rarity.equalsIgnoreCase("SPARKING");
	}
	
	public boolean isExtreme() {
		return this.rarity.equalsIgnoreCase("EXTREME");
	}
	
	public boolean isHero() {
		return this.rarity.equalsIgnoreCase("HERO");
	}
	
	public String getBaseName() {
		return baseName;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = "https://dblegends.net/" + imageLink;
	}

	public void setSiteID(int siteID) {
		this.siteID = siteID;
	}

	public void setZenkai(boolean isZenkai) {
		this.isZenkai = isZenkai;
	}

	public String getCharacterName() {
		return characterName;
	}

	public String getRarity() {
		return rarity;
	}

	public Color getColour() {
		Color colour = Color.black;
		if (this.colour.equalsIgnoreCase("red"))
			colour = Color.red;
		else if (this.colour.equalsIgnoreCase("yel"))
			colour = Color.yellow;
		else if (this.colour.equalsIgnoreCase("grn"))
			colour = Color.green;
		else if (this.colour.equalsIgnoreCase("blu"))
			colour = Color.blue;
		else if (this.colour.equalsIgnoreCase("pur"))
			colour = Color.magenta;
		else if (this.colour.equalsIgnoreCase("lgt"))
			colour = Color.white;

		return colour;
	}

	public String getGameID() {
		return gameID;
	}

	public String getImageLink() {
		return imageLink.strip();
	}

	public String getPageLink() {
		return "https://dblegends.net/character.php?id=" + siteID;
	}

	public int getSiteID() {
		return siteID;
	}

	public boolean isZenkai() {
		return isZenkai;
	}

	@Override
	public String toString() {
		return "Characters [\ncharacterName=" + characterName + "\nrarity=" + rarity + "\ngameID=" + gameID + "\nsiteID="
				+ siteID + "\n]";
	}

	public void setBaseName(String baseName) {
		if(baseName == null)
			return;
		baseName = capitalize(baseName);
		if (this.baseName == null)
			this.baseName = baseName;
		else
			this.baseName += " & " + baseName;
	}
	
	public static String capitalize(String input) {
	    String[] parts = input.split("_");
	    StringBuilder result = new StringBuilder();
	    for (String part : parts) {
	        result.append(Character.toUpperCase(part.charAt(0)));
	        result.append(part.substring(1)).append(" ");
	    }
	    return result.toString().trim();
	}
}