package download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.ProgressMonitorInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fileops.FileOperations;
import utils.BytesProgressBar;
import utils.ProgressBar;

public class Downloader {
	protected static final Logger logger = LogManager.getLogger(Downloader.class.getName());
	private static AtomicLong totalBytesRead = new AtomicLong(0);
	private static AtomicLong totalFileSize = new AtomicLong(0);

	private static final int MAX_RETRIES = 3;
	private static ProgressBar progressBar;

	public static void setProgressBar(ProgressBar progressBar) {
		Downloader.progressBar = progressBar;
	}

	public static long download(String link, String destination) {
		int timeoutMillis = 10000;
		long fileSize = 0;
		int attempt = 0;

		while (attempt < MAX_RETRIES) {
			attempt++;
			int bytesRead = 0;
			try {
				URL url = new URL(link);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(timeoutMillis);
				connection.setReadTimeout(timeoutMillis);
				fileSize = connection.getContentLength();

				// Ensure the directory structure exists
				File destinationFile = new File(destination);
				FileOperations.createDirectory(destinationFile.getParent());

				try (ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(null, "Downloading " + link,
						new BufferedInputStream(url.openStream()));
						FileOutputStream fos = new FileOutputStream(destinationFile)) {

					byte[] buffer = new byte[1024];

					while ((bytesRead = pmis.read(buffer)) != -1) {
						fos.write(buffer, 0, bytesRead);
						totalBytesRead.addAndGet(bytesRead);
						progressBar.updateProgress();
					}

					// Verify the downloaded file size
					if (destinationFile.length() == fileSize) {
						logger.info("Downloaded: \n\"{}\"", destination);
						return fileSize;
					}
				} catch (SocketException e) {
				} catch (Exception e) {
				}
			} catch (Exception e) {
			}
			totalBytesRead.addAndGet(-bytesRead);
		}

		// If max retries reached, delete the file and adjust total file size
		File failedFile = new File(destination);
		if (failedFile.exists()) {
			failedFile.delete();
		}
		totalFileSize.addAndGet(-fileSize);
		System.out.println("Failed to download " + destination + " after " + MAX_RETRIES + " attempts.");
		return 0;
	}

	public static long getFileSize(String link) {
		try {
			int timeoutMillis = 3000;
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(timeoutMillis);
			connection.setReadTimeout(timeoutMillis);
			connection.connect();

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return connection.getContentLengthLong();
			}
		} catch (IOException e) {
			// Handle exceptions
		}
		return -1;
	}

	public static Set<String> openAllLinks(List<String[]> links) throws InterruptedException {
		Set<String> modifiedPaths = new HashSet<>();

		if (links.isEmpty())
			return modifiedPaths;

		AtomicLong startTime = new AtomicLong(0);
		ProgressBar progressBar = new BytesProgressBar(totalBytesRead, totalFileSize, startTime);
		Downloader.setProgressBar(progressBar);

		ExecutorService fileSizeExecutor = Executors.newFixedThreadPool(50); // 50 threads for getting file sizes
		ExecutorService openExecutor = Executors.newFixedThreadPool(10); // 10 threads for opening web pages

		List<Callable<Long>> sizeTasks = links.stream().map(link -> (Callable<Long>) () -> getFileSize(link[0]))
				.toList();

		try {
			System.out.println("Calculating total size...");
			List<Future<Long>> sizeFutures = fileSizeExecutor.invokeAll(sizeTasks);

			for (Future<Long> future : sizeFutures) {
				try {
					long fileSize = future.get();
					if (fileSize > 0) {
						totalFileSize.addAndGet(fileSize);
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

			List<Callable<Void>> openTasks = links.stream().map(link -> (Callable<Void>) () -> {
				long fileSize = download(link[0], link[1]);
				if (fileSize > 0) {
					modifiedPaths.add(link[1]);
				}
				return null;
			}).toList();

			startTime.set(System.currentTimeMillis());
			openExecutor.invokeAll(openTasks);
		} finally {
			fileSizeExecutor.shutdown();
			openExecutor.shutdown();
			fileSizeExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			openExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		}

		progressBar.printTimeTaken();
		System.out.println();

		return modifiedPaths;
	}
}
