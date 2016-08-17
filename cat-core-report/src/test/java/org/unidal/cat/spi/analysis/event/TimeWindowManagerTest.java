package org.unidal.cat.spi.analysis.event;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

public class TimeWindowManagerTest extends ComponentTestCase {
	private void check(MockTimeWindowManager manager, MockTimeWindowHandler handler, int minutes, String expected)
	      throws InterruptedException {
		manager.tick(minutes);
		TimeUnit.MILLISECONDS.sleep(3);

		Assert.assertEquals(expected, handler.getResult());
	}

	@Test
	public void testAbnormal() throws Exception {
		MockTimeWindowManager manager = new MockTimeWindowManager(405605);
		MockTimeWindowHandler handler = new MockTimeWindowHandler();

		manager.register(handler);

		Threads.forGroup("Test").start(manager);

		check(manager, handler, 0, "+405605"); // start 405605
		check(manager, handler, 200, "+405608-405605"); // start 405608, and stop 405605
		check(manager, handler, 200, "+405611-405608"); // start 405611, and stop 405608
		check(manager, handler, 200, "+405615-405611"); // start 405615, and stop 405611
		check(manager, handler, 57, "+405616"); // start 405616
		check(manager, handler, 6, "-405615"); // stop 405615

		manager.shutdown();
	}

	@Test
	public void testNormal() throws Exception {
		MockTimeWindowManager manager = new MockTimeWindowManager(405605);
		MockTimeWindowHandler handler = new MockTimeWindowHandler();

		manager.register(handler);

		Threads.forGroup("Test").start(manager);

		check(manager, handler, 0, "+405605"); // start 405605
		check(manager, handler, 30, "");
		check(manager, handler, 27, "+405606"); // start 405606
		check(manager, handler, 6, "-405605"); // stop 405605
		check(manager, handler, 54, "+405607"); // start 405607
		check(manager, handler, 6, "-405606"); // stop 405606
		check(manager, handler, 54, "+405608"); // start 405608

		manager.shutdown();
	}

	class MockTimeWindowHandler implements TimeWindowHandler {
		private StringBuilder m_sb = new StringBuilder(256);

		public String getResult() {
			String result = m_sb.toString();

			m_sb.setLength(0);
			return result;
		}

		@Override
		public void onTimeWindowEnter(int hour) {
			m_sb.append("+" + hour);
		}

		@Override
		public void onTimeWindowExit(int hour) {
			m_sb.append("-" + hour);
		}
	}

	class MockTimeWindowManager extends DefaultTimeWindowManager {
		private long m_timestamp;

		public MockTimeWindowManager(int hour) {
			m_timestamp = TimeUnit.HOURS.toMillis(hour);
		}

		@Override
		protected long getCurrentTimeMillis() {
			return m_timestamp;
		}

		@Override
		protected void sleep() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(1);
		}

		public void tick(int minutes) {
			m_timestamp += TimeUnit.MINUTES.toMillis(minutes);
		}
	}
}
