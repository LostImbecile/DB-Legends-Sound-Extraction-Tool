package main;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Run {
	private static String[] args;

	@SuppressWarnings("resource")
	public static void main(String[] args)  {
		Run.setArgs(args);
		try {
			runInConsole(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nPress enter to continue...");
		new Scanner(System.in).nextLine();
	}

	public static void runInConsole(String[] args) throws IOException {
		/*
		 * Runs cmd through another cmd and launches the bot
		 * for info go here:
		 * https://learn.microsoft.com/en-us/windows-server/administration/windows-
		 * commands/cmd
		 * 
		 */
		Console console = System.console();

		// If the bot isn't already in a console it runs the main method
		if (console == null && !GraphicsEnvironment.isHeadless() && getJarName().contains(".jar")) {
			try {
				Runtime.getRuntime().exec(getRunInConsoleCommand());
			} catch (Exception e) {
				// If you're not on windows just run the bot through the
				// terminal or create a shell script for it.
				System.exit(0);
			}
		} else {
			// Arguments you send are handed down to the main class normally
			Main.main(args);
		}
	}

	public static String getCmdTitle() {
		String title = "DBL Sort";
		for (String arg : args) {
			if (arg.toLowerCase().contains("title:")) {
				title = arg.replaceAll("[-\"]", "").replaceAll("(?i)title:", "");
				break;
			}
		}
		if (title.isBlank()) {
			title = "DBL Sort";
		}

		return title;
	}

	public static String[] getRunInConsoleCommand() {
		return new String[] { "cmd", "/K", "Start", "\"" + getCmdTitle() + "\"", "java", "-Xms100m", "-Xmx800m", "-jar",
				getJarName(), String.join(" ", args), "&& exit" };
	}

	public static String[] getRunCommand() {
		return new String[] { "java", "-Xms100m", "-Xmx800m", "-jar", getJarName(), String.join(" ", args) };
	}

	public static String getJarName() {
		String jar;
		try {
			jar = new File(Run.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getName();
		} catch (URISyntaxException e) {
			// default
			jar = "dbl.jar";
		}
		return jar;
	}

	public static String[] getArgs() {
		return args;
	}

	public static void setArgs(String[] args) {
		Run.args = args;
	}

}
