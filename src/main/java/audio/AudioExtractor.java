package audio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import config.ConfigManager;
import execute.command.CommandExecuter;
import fileops.FileOperations;
import utils.NumberProgressBar;
import utils.ProgressBar;

public class AudioExtractor {
	protected static final Logger logger = LogManager.getLogger(AudioExtractor.class.getName());
	private static AtomicLong totalFileNum = new AtomicLong(0);
	private static AtomicLong processedNum = new AtomicLong(0);
	private static AtomicLong startTime = new AtomicLong(0);
	private static String ffmpegPath = "ffmpeg";
	private static String soxPath = "sox";
	private static String vgmstreamPath = "vgmstream-cli";
	private static FileOperations inputFolderIndexer = new FileOperations();
	private static FileOperations mainFolderIndexer = new FileOperations();
	private static ProgressBar progressBar;
	private static Set<String> modifiedPaths = new HashSet<>();
	private static boolean useSox = false;

	private AudioExtractor() {
	}

	public static void setProgressBar(ProgressBar progressBar) {
		AudioExtractor.progressBar = progressBar;
	}

	public static Set<String> extractAndConvert(String inputDir) {
		boolean extract = ConfigManager.getBooleanProperty("Extract_Files");
		boolean convertToOGG = ConfigManager.getBooleanProperty("Convert_WAV_To_OGG");
		int numberOfThreads = Runtime.getRuntime().availableProcessors();
		Path inputPath = Paths.get(inputDir);

		ProgressBar progressBar = new NumberProgressBar(processedNum, totalFileNum, startTime);
		setProgressBar(progressBar);

		indexMainFolder(extract, convertToOGG, inputPath);
		progressBar.reset();
		extractFiles(inputPath, extract, numberOfThreads);
		progressBar.reset();
		convertFiles(inputPath, convertToOGG, numberOfThreads);

		return modifiedPaths;
	}

	public static void indexMainFolder(boolean extract, boolean convertToOGG, Path inputPath) {
		if (convertToOGG || extract) {
			try {
				mainFolderIndexer.indexDirectory(Paths.get("."), 4, inputPath);
			} catch (IOException e) {
				mainFolderIndexer = null;
			}
		}
	}

	public static void convertFiles(Path inputPath, boolean convertToOGG, int numberOfCores) {
		if (convertToOGG && checkWavConverter()) {
			try {
				System.out.println("Indexing for conversion...");
				inputFolderIndexer.indexDirectory(inputPath);
			} catch (IOException e) {
				logger.error(e);
				return;
			}
			totalFileNum.set(inputFolderIndexer.numberOfFilesWithExtension("wav"));
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

	public static void extractFiles(Path inputPath, boolean extract, int numberOfCores) {
		if (extract) {
			try {
				System.out.println("Indexing for extraction...");
				inputFolderIndexer.indexDirectory(inputPath);
			} catch (IOException e) {
				logger.error(e);
				return;
			}

			System.out.println("Extracting files...");

			if (!checkVgmstream())
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

	private static boolean checkVgmstream() {
		String path = getExecutablePath(vgmstreamPath);
		if (path == null) {
			System.out.println(
					"vgmstream-cli.exe not found anywhere.\nPlease put it within a folder inside the directory or system path and rerun");
			return false;
		}
		vgmstreamPath = path;
		return true;
	}

	private static boolean checkWavConverter() {
		String path = getExecutablePath(ffmpegPath);
		if (path != null) {
			ffmpegPath = path;
			useSox = false;
			return true;
		}
		
		path = getExecutablePath(soxPath);
		if (path != null) {
			soxPath = path;
			useSox = true;
			return true;
		}
		
		System.out.println(
				"Neither FFmpeg.exe nor sox.exe found.\nPlease put one of them within a folder inside the directory or system path and rerun");
		return false;
	}

	public static String getExecutablePath(String command) {
		if (!CommandExecuter.canExecuteCommand(command)) {
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
			String command;
			if (useSox) {
				command = String.format("\"%s\" \"%s\" \"%s\"", soxPath, file.getAbsolutePath(), outputFilePath);
			} else {
				command = String.format("\"%s\" -n -i \"%s\" \"%s\"", ffmpegPath, file.getAbsolutePath(),
						outputFilePath);
			}
			if (CommandExecuter.executeCommand(command))
				modifiedPaths.add(file.getPath());
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
			if (CommandExecuter.executeCommand(command))
				modifiedPaths.add(file.getPath());
		}
		updateProgress();
	}

	private static void convertAwbToWav(File file) {
		String nameNoExt = file.getName();
		nameNoExt = nameNoExt.substring(0, nameNoExt.indexOf("."));
		if (inputFolderIndexer != null && !inputFolderIndexer.containsFileName(nameNoExt, "awb")) {
			String command = String.format("\"%s\" -S 0 \"%s\"", vgmstreamPath, file.getAbsolutePath());
			if (CommandExecuter.executeCommand(command))
				modifiedPaths.add(file.getPath());
		}
		updateProgress();
	}

	@FunctionalInterface
	private interface FileProcessor {
		void process(File file);
	}
}