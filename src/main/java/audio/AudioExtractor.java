package audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.ConfigManager;
import fileops.FileOperations;
import utils.NumberProgressBar;
import utils.ProgressBar;

public class AudioExtractor {
	protected static final Logger logger = LogManager.getLogger(AudioExtractor.class.getName());
	private static AtomicLong totalFileNum = new AtomicLong(0);
	private static AtomicLong processedNum = new AtomicLong(0);
	private static AtomicLong startTime = new AtomicLong(0);
	private static String ffmpegPath = "ffmpeg";
	private static String vgmstreamPath = "vgmstream-cli";
	private static FileOperations inputFolderIndexer = new FileOperations();
	private static FileOperations mainFolderIndexer = new FileOperations();
	private static ProgressBar progressBar;

	private AudioExtractor() {
	}

	public static void setProgressBar(ProgressBar progressBar) {
		AudioExtractor.progressBar = progressBar;
	}

	public static void extractAndConvert(String inputDir) {
		boolean extract = ConfigManager.getBooleanProperty("Extract_Files");
		boolean convertToOGG = ConfigManager.getBooleanProperty("Convert_WAV_To_OGG");
		int numberOfThreads = Runtime.getRuntime().availableProcessors();

		
		ProgressBar progressBar = new NumberProgressBar(processedNum, totalFileNum, startTime);
		setProgressBar(progressBar);

		try {
			mainFolderIndexer.indexDirectory(Paths.get("."));
		} catch (IOException e) {
			mainFolderIndexer = null;
		}
		progressBar.reset();
		extractFiles(inputDir, extract, numberOfThreads);
		progressBar.reset();
		convertFiles(inputDir, convertToOGG, numberOfThreads);
		deleteFiles(convertToOGG);

	}

	public static void deleteFiles(boolean convertToOGG) {
		boolean deleteWAV = ConfigManager.getBooleanProperty("Delete_WAV");
		boolean deleteACB = ConfigManager.getBooleanProperty("Delete_ACB");
		boolean deleteAWB = ConfigManager.getBooleanProperty("Delete_AWB");

		if (!convertToOGG)
			deleteWAV = false;
		List<String> deleteExt = new ArrayList<>();
		if (deleteWAV)
			deleteExt.add(".wav");
		if (deleteACB)
			deleteExt.add(".acb");
		if (deleteAWB)
			deleteExt.add(".awb");

		if (deleteWAV || deleteACB || deleteAWB) {
			System.out.println("Deleting files...");
			inputFolderIndexer.deleteFiles(deleteExt);
		}
	}

	public static void convertFiles(String inputDir, boolean convertToOGG, int numberOfCores) {
		if (convertToOGG && checkVgmstream()) {
			try {
				inputFolderIndexer.indexDirectory(Paths.get(inputDir));
			} catch (IOException e) {
				logger.error(e);
			}
			totalFileNum.set(inputFolderIndexer.numberOfFilesWithExtension("wav"));
			// Step 2: Convert all .wav files to .ogg
			System.out.println("Converting wav to ogg...");
			ExecutorService wavExecutor = Executors.newFixedThreadPool(numberOfCores);
			processFiles(wavExecutor, ".wav", AudioExtractor::convertWavToOgg);
			wavExecutor.shutdown();
			try {
				wavExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressBar.printTimeTaken();
			System.out.println();
		}
	}

	public static void extractFiles(String inputDir, boolean extract, int numberOfCores) {
		if (extract) {
			try {
				inputFolderIndexer.indexDirectory(Paths.get(inputDir));
			} catch (IOException e) {
				logger.error(e);
			}

			// Step 1: Extract all .acb files to .wav
			System.out.println("Extracting files...");

			if (!checkFFmpeg())
				return;

			totalFileNum.set(inputFolderIndexer.numberOfFilesWithExtension("acb", "awb"));

			ExecutorService acbExecutor = Executors.newFixedThreadPool(numberOfCores);
			processFiles(acbExecutor, ".acb", AudioExtractor::convertAcbToWav);
			acbExecutor.shutdown();
			try {
				acbExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			ExecutorService awbExecutor = Executors.newFixedThreadPool(numberOfCores);
			processFiles(awbExecutor, ".awb", AudioExtractor::convertAwbToWav);
			awbExecutor.shutdown();
			try {
				awbExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressBar.printTimeTaken();
			System.out.println();
		}
	}

	private static boolean checkFFmpeg() {
		if (getExecutablePath(ffmpegPath) == null) {
			System.out.println(
					"FFmpeg.exe not found anywhere.\nPlease put it within a folder inside the directory or system path and rerun");
			return false;
		}

		return true;
	}

	private static boolean checkVgmstream() {
		if (getExecutablePath(vgmstreamPath) == null) {
			System.out.println(
					"vgmstream-cli.exe not found anywhere.\nPlease put it within a folder inside the directory or system path and rerun");
			return false;
		}

		return true;
	}

	public static String getExecutablePath(String command) {
		if (!canExecuteCommand(command)) {
			return mainFolderIndexer != null ? mainFolderIndexer.getPath(command + ".exe") : null;
		}
		return command;
	}

	private static void processFiles(ExecutorService executor, String extension, FileProcessor processor) {
		for (Entry<String, Path> entry : inputFolderIndexer.getFileIndex().entrySet()) {
			File file = entry.getValue().toFile();
			if (!file.isDirectory() && file.getName().endsWith(extension)) {
				executor.submit(() -> processor.process(file));
			}
		}
	}

	private static void convertWavToOgg(File file) {
		String outputFilePath = file.getAbsolutePath().replaceAll("\\.wav$", ".ogg");
		if (!new File(outputFilePath).exists()) {
			String command = String.format("\"%s\" -n -i \"%s\" \"%s\"", ffmpegPath, file.getAbsolutePath(),
					outputFilePath);
			executeCommand(command);
		}
		updateProgress();
	}

	public static void updateProgress() {
		processedNum.addAndGet(1);
		progressBar.updateProgress();
	}

	private static void convertAcbToWav(File file) {
		String nameNoExt = file.getName();
		nameNoExt = nameNoExt.substring(0, nameNoExt.indexOf("."));
		if (inputFolderIndexer != null && !inputFolderIndexer.containsFileName(nameNoExt, "acb")) {
			String outputFilePath = file.getAbsolutePath().replaceAll("\\.acb$", "_?03s_?n.wav");
			String command = String.format("\"%s\" -S 0 -o \"%s\" \"%s\"", vgmstreamPath, outputFilePath,
					file.getAbsolutePath());
			executeCommand(command);
		}
		updateProgress();
	}

	private static void convertAwbToWav(File file) {
		String nameNoExt = file.getName();
		nameNoExt = nameNoExt.substring(0, nameNoExt.indexOf("."));
		if (inputFolderIndexer != null && !inputFolderIndexer.containsFileName(nameNoExt, "awb")) {
			String command = String.format("\"%s\" -S 0 \"%s\"", vgmstreamPath, file.getAbsolutePath());
			executeCommand(command);
		}
		updateProgress();
	}

	private static boolean canExecuteCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.destroy();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private static void executeCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);

			// Consume the output and error streams to prevent blocking
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
			outputGobbler.start();
			errorGobbler.start();

			int exitCode = process.waitFor();
			outputGobbler.join();
			errorGobbler.join();

			if (exitCode != 0) {
				logger.error("{}%n-> Failed with exit code: {}%n", command, exitCode);
			}
		} catch (IOException | InterruptedException e) {
			logger.error(e);
		}
	}

	private static class StreamGobbler extends Thread {
		private final BufferedReader reader;

		public StreamGobbler(InputStream inputStream) {
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
		}

		@Override
		public void run() {
			try {
				while (reader.readLine() != null) {
					// consume
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	@FunctionalInterface
	private interface FileProcessor {
		void process(File file);
	}

}
