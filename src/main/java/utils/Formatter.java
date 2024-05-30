package utils;

public class Formatter {

	public static String formatTime(long time) {
		long seconds = time / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

}