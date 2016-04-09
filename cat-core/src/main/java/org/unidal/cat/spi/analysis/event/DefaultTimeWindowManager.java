package org.unidal.cat.spi.analysis.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Named;

@Named(type = TimeWindowManager.class)
public class DefaultTimeWindowManager implements TimeWindowManager, Task {
	private static final long MINUTE = 60 * 1000L;

	private static final long HOUR = 60 * MINUTE;

	private List<TimeWindowHandler> m_handlers = new ArrayList<TimeWindowHandler>();

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	private long m_lastStartTime;

	private long m_lastEndTime;

	private long compute(long now) {
		long startTime = now - now % HOUR;

		// for current period
		if (startTime > m_lastStartTime) {
			m_lastStartTime = startTime;
			return startTime;
		}

		// prepare next period ahead
		if (now - m_lastStartTime >= HOUR - 3 * MINUTE) {
			m_lastStartTime = startTime + HOUR;
			return startTime + HOUR;
		}

		// last period is over
		if (now - m_lastEndTime >= HOUR + 3 * MINUTE) {
			long lastEndTime = m_lastEndTime;

			m_lastEndTime = startTime;
			return -lastEndTime;
		}

		return 0;
	}

	private void enterTimeWindow(int hour) {
		for (TimeWindowHandler handler : m_handlers) {
			handler.onTimeWindowEnter(hour);
		}
	}

	private void exitTimeWindow(int hour) {
		for (TimeWindowHandler handler : m_handlers) {
			handler.onTimeWindowExit(hour);
		}
	}

	protected long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void register(TimeWindowHandler handler) {
		if (!m_handlers.contains(handler)) {
			m_handlers.add(handler);
		}
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				long time = compute(getCurrentTimeMillis());

				if (time > 0) {
					int hour = (int) TimeUnit.MILLISECONDS.toHours(time);

					enterTimeWindow(hour);
				} else if (time < 0) {
					int hour = (int) TimeUnit.MILLISECONDS.toHours(-time);

					exitTimeWindow(hour);
				}

				sleep();
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);
	}

	protected void sleep() throws InterruptedException {
		TimeUnit.SECONDS.sleep(1);
	}
}
