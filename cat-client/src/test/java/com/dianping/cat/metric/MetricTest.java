package com.dianping.cat.metric;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;

public class MetricTest {

	@Before
	public void before() {
		Cat.initializeByDomain("cat");
	}

	@Test
	public void test() throws InterruptedException {
		int j = 0;
		while (true && j < 100) {
			j++;
			for (int i = 0; i < 10000; i++) {
				for (int index = 0; index < 20; index++) {
					Cat.logMetricForCount("key." + index);
				}
			}
			Thread.sleep(1000);
		}
	}

	@Test
	public void testThreads() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			Threads.forGroup("cat").start(new SendThread());
		}

		Thread.sleep(60 * 1000 * 1000);
	}

	public static class SendThread implements Task {

		@Override
		public void run() {
			int j = 0;
			for (int index = 0; index < 20; index++) {
				Cat.setMetricSlowThreshold("duration." + index, 10);
			}

			while (true && j < 100) {
				j++;
				for (int i = 0; i < 10000; i++) {
					for (int index = 0; index < 20; index++) {
						Cat.logMetricForCount("key." + index);
						Cat.logMetricForDuration("duration." + index, index);
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public String getName() {
			return "sender thread";
		}

		@Override
		public void shutdown() {
		}
	}

}
