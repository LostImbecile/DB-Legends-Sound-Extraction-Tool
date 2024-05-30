package fileops;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtilities {
	protected static final Logger logger = LogManager.getLogger(FileUtilities.class.getName());

	public static void createDirectory(String dirName) {
		try {
			File directory = new File(dirName);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		} catch (Exception e) {
			logger.error("Couldn't create directory.");
		}
	}

	public static OutputStream getFileOutputStream(String fileName) {
		try {
			return new FileOutputStream(fileName);
		} catch (IOException e) {
			logger.error("Failed to get output stream.", e);
			return null;
		}
	}

	public static InputStream getFileInputStream(String fileName, boolean createFileIfNotFound) {
		InputStream input;
		File file = new File(fileName);

		if (file.exists()) {
			input = getNewFile(file);

		} else {

			input = FileUtilities.class.getResourceAsStream("/" + fileName);

			if (input == null && createFileIfNotFound) {
				input = getNewFile(file);
			}
		}

		return input;
	}

	public static boolean isFileExist(String fileName) {
		return new File(fileName).exists();
	}

	private static InputStream getNewFile(File file) {
		try {
			if (!file.createNewFile()) {
				if (file.exists())
					return new FileInputStream(file);
				return null;
			} else {
				return new FileInputStream(file);
			}
		} catch (Exception e) {
			logger.error("Failed to create new file.", e);
			return null;
		}
	}

	public static String readInputStream(InputStream is) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			logger.error("Failed to read input stream.", e);
		}

		return result.toString();
	}

	public static String readInputStream(InputStream is, String joinLinesWith) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = getBufferedReader(is)) {
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line + joinLinesWith);
			}
		} catch (IOException e) {
			logger.error("Failed to read input stream.", e);
		}

		return result.toString();
	}

	public static String readURL(String link) {
		try {
			return FileUtilities.readInputStream(urlAsInputStream(link), "\n");
		} catch (IOException e) {
			return "failed";
		}
	}

	public static InputStream urlAsInputStream(String address) throws IOException {
		return new URL(address).openStream();
	}

	public static void writeToFile(String txt, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			writer.write(txt);
		} catch (IOException e) {
			logger.error("Failed to write string to file.", e);
		}
	}

	public static BufferedReader getBufferedReader(InputStream is) {
		return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	}

	public static InputStream toInputStream(String st) {
		return IOUtils.toInputStream(st, StandardCharsets.UTF_8);
	}

	public static InputStream toInputStream(List<String> stringList) {
		String concatenatedString = String.join("\n", stringList);
		return toInputStream(concatenatedString);
	}

	public static BufferedInputStream toBufferedInputStream(List<String> list) {
		return new BufferedInputStream(toInputStream(list));
	}

	public static BufferedInputStream toBufferedInputStream(String st) {
		return new BufferedInputStream(toInputStream(st));
	}

	public static BufferedInputStream toBufferedInputStream(InputStream is) {
		return new BufferedInputStream(is);
	}

}
