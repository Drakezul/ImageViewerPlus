package de.grueb.crop.UI;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedTask {

	private ScheduledExecutorService executor;
	private Future<?> task;
	private long intervalSeconds;
	private Runnable command;

	public TimedTask(int intervalSeconds, Runnable command) {
		this.intervalSeconds = intervalSeconds;
		this.command = command;
		executor = Executors.newScheduledThreadPool(1);
	}

	public TimedTask(int intervalSeconds) {
		this(intervalSeconds, null);
	}

	/**
	 * @return true if the task has been started, false if a task is still running
	 *         in this instance
	 */
	public final boolean start() {
		if (task == null || task.isCancelled() || task.isDone()) {
			task = executor.scheduleAtFixedRate(this.command, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
			return true;
		}
		return false;
	}

	/**
	 * @return true if the task is stopped, false if the task was stopped already
	 */
	public final boolean stop() {
		if (task != null && !task.isCancelled() && !task.isDone()) {
			task.cancel(false);
			return true;
		}
		return false;
	}

	public boolean isRunning() {
		return task != null && !task.isDone() && !task.isCancelled();
	}

	public boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 
	 * @param unit
	 *            the requested unit
	 * @return the task interval converted to the given {@link TimeUnit}
	 */
	public long getInterval(TimeUnit unit) {
		return unit.convert(intervalSeconds, TimeUnit.SECONDS);
	}

	/**
	 * @return the task interval in seconds
	 */
	public long getInterval() {
		return this.intervalSeconds;
	}

	public void setInterval(TimeUnit unit, long interval) {
		this.intervalSeconds = TimeUnit.SECONDS.convert(interval, unit);
	}

	public void setInterval(long interval) {
		this.intervalSeconds = interval;
	}

	public void setCommad(Runnable command) {
		this.command = command;
	}

}
