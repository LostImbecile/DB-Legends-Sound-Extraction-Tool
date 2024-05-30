package utils;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ProgressBar {
	protected int barSize = 50;
	protected AtomicLong startTime;
	protected boolean printed = false;

	ProgressBar() {
	}

	public abstract void updateProgress();

	protected abstract int getPercent();

	protected abstract String getProgressInfo();

	public void printTimeTaken() {
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime.get();
		if (printed)
			System.out.println();
		System.out.println("Time taken: " + Formatter.formatTime(timeTaken));
	}

	protected void printProgressBar() {
		printed = true;
		int percent = getPercent();
		int progressSize = (int) (((double) percent / 100) * barSize);
		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < barSize; i++) {
			if (i < progressSize) {
				bar.append("#");
			} else {
				bar.append(" ");
			}
		}
		bar.append("] ").append(percent).append("% ").append(getProgressInfo());
		long timeElapsed = System.currentTimeMillis() - startTime.get();
		if (percent > 0) {
			long timeRemaining = (timeElapsed / percent) * (100 - percent);
			bar.append(" (Estimated time remaining: ").append(Formatter.formatTime(timeRemaining)).append(")");
		} else {
			bar.append(" (Estimated time remaining: unknown)");
		}
		System.out.print("\r" + bar.toString() + "\u0008".repeat(100));
		System.out.flush();
	}

	public abstract void reset();
}
