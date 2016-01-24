package org.unidal.cat.spi.task;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.ComponentTestCase;

public class TaskManagerTest extends ComponentTestCase {
	@Test
	public void testPropagation() throws Exception {
		TaskManager manager = lookup(TaskManager.class);
		MockHourTaskConsumer hour = new MockHourTaskConsumer();
		MockDayTaskConsumer day = new MockDayTaskConsumer();
		MockWeekTaskConsumer week = new MockWeekTaskConsumer();
		CounterTaskConsumer counter = new CounterTaskConsumer();

		manager.subscribe("mock.hour", hour);
		manager.subscribe("mock.day", day);
		manager.subscribe("mock.week", week);
		manager.subscribe("*", counter);

		manager.start();

		// all consumer instances produce tasks
		for (int i = 0; i < 3; i++) {
			manager.addTask(new TaskPayload("mock.hour", ReportPeriod.HOUR, new Date()));
		}

		manager.awaitTermination(10, TimeUnit.MILLISECONDS);

		Assert.assertEquals(6, counter.getCount());
		Assert.assertEquals(1, hour.getCount());
		Assert.assertEquals(1, day.getCount());
		Assert.assertEquals(1, week.getCount());
	}

	private static class CounterTaskConsumer implements TaskConsumer {
		private AtomicInteger m_count = new AtomicInteger();

		public int getCount() {
			return m_count.get();
		}

		@Override
		public TaskStatus onTask(TaskManager manager, TaskPayload payload) {
			m_count.incrementAndGet();
			return TaskStatus.SUCCESSFUL;
		}
	}

	private static class MockDayTaskConsumer implements TaskConsumer {
		private AtomicInteger m_count = new AtomicInteger();

		private ConcurrentMap<String, String> m_done = new ConcurrentHashMap<String, String>();

		public int getCount() {
			return m_count.get();
		}

		@Override
		public TaskStatus onTask(TaskManager manager, TaskPayload payload) {
			String key = payload.getKey();

			if (m_done.containsKey(key)) {
				return TaskStatus.SKIPPED;
			} else {
				Date startTime = ReportPeriod.DAY.parse(key, null);

				m_done.putIfAbsent(key, key);
				m_count.incrementAndGet();

				manager.addTask(new TaskPayload("mock.week", ReportPeriod.WEEK.format(startTime)));
				manager.addTask(new TaskPayload("mock.month", ReportPeriod.MONTH.format(startTime)));
				return TaskStatus.SUCCESSFUL;
			}
		}
	}

	private static class MockHourTaskConsumer implements TaskConsumer {
		private AtomicInteger m_count = new AtomicInteger();

		private ConcurrentMap<String, String> m_done = new ConcurrentHashMap<String, String>();

		public int getCount() {
			return m_count.get();
		}

		@Override
		public TaskStatus onTask(TaskManager manager, TaskPayload payload) {
			String key = payload.getKey();

			if (m_done.containsKey(key)) {
				return TaskStatus.SKIPPED;
			} else {
				Date startTime = ReportPeriod.HOUR.parse(key, null);

				m_done.putIfAbsent(key, key);
				m_count.incrementAndGet();

				manager.addTask(new TaskPayload("mock.day", ReportPeriod.DAY.format(startTime)));
				return TaskStatus.SUCCESSFUL;
			}
		}
	}

	private static class MockWeekTaskConsumer implements TaskConsumer {
		private AtomicInteger m_count = new AtomicInteger();

		private ConcurrentMap<String, String> m_done = new ConcurrentHashMap<String, String>();

		public int getCount() {
			return m_count.get();
		}

		@Override
		public TaskStatus onTask(TaskManager manager, TaskPayload payload) {
			String key = payload.getKey();

			if (m_done.containsKey(key)) {
				return TaskStatus.SKIPPED;
			} else {
				m_done.putIfAbsent(key, key);
				m_count.incrementAndGet();
				return TaskStatus.SUCCESSFUL;
			}
		}
	}
}
