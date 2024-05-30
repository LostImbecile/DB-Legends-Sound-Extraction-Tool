package utils;

import java.util.concurrent.atomic.AtomicLong;

public class NumberProgressBar extends ProgressBar {
    private AtomicLong processedNum;
    private AtomicLong totalNum;

    public NumberProgressBar(AtomicLong processedNum, AtomicLong totalNum, AtomicLong startTime) {
        this.processedNum = processedNum;
        this.totalNum = totalNum;
        this.startTime = startTime;
    }

    @Override
    public void updateProgress() {
        printProgressBar();
    }

    @Override
    protected int getPercent() {
        return (int) (((double) processedNum.get() / totalNum.get()) * 100);
    }

    @Override
    protected String getProgressInfo() {
        return String.format("(%d/%d)", processedNum.get(), totalNum.get());
    }

	@Override
	public void reset() {
		startTime.set(System.currentTimeMillis());
		printed = false;
		processedNum.set(0);
	}
}
