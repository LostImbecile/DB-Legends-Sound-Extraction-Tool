package utils;

import java.util.concurrent.atomic.AtomicLong;

public class BytesProgressBar extends ProgressBar {
	private AtomicLong totalBytesRead;
	private AtomicLong totalFileSize;

	public BytesProgressBar(AtomicLong totalBytesRead, AtomicLong totalFileSize, AtomicLong startTime) {
		this.totalBytesRead = totalBytesRead;
		this.totalFileSize = totalFileSize;
		this.startTime = startTime;
	}

	@Override
	public void updateProgress() {
		printProgressBar();
	}

	@Override
	protected int getPercent() {
		return (int) (((double) totalBytesRead.get() / totalFileSize.get()) * 100);
	}

	@Override
	protected String getProgressInfo() {
		double totalInBytes = totalFileSize.get();
		double readInBytes = totalBytesRead.get();
		double totalInMB = totalInBytes / (1024 * 1024);
		double readInMB = readInBytes / (1024 * 1024);
		String unit = "MB";
		if (totalInBytes > 1024 * 1024 * 1024) {
			totalInMB = totalInBytes / (1024 * 1024 * 1024);
			readInMB = readInBytes / (1024 * 1024 * 1024);
			unit = "GB";
		}
		return String.format("(%.1f/%.1f %s)", readInMB, totalInMB, unit);
	}

	@Override
	public void reset() {
		startTime.set(System.currentTimeMillis());
		printed = false;
		totalBytesRead.set(0);
	}
}
