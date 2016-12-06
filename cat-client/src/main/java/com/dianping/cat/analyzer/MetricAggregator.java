package com.dianping.cat.analyzer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class MetricAggregator {

	private static ConcurrentHashMap<String, MetricItem> s_metrics = new ConcurrentHashMap<String, MetricItem>();

	private static ConcurrentHashMap<String, Integer> s_metricThresholds = new ConcurrentHashMap<String, Integer>();

	public static void addCountMetric(String key, int value) {
		makeSureMetricExist(key).m_count.addAndGet(value);
	}

	public static void addTimerMetric(String key, long mills) {
		MetricItem item = makeSureMetricExist(key);

		item.m_count.incrementAndGet();
		item.m_sum.addAndGet(mills);

		if (item.m_slowThreshold > 0 && mills > item.m_slowThreshold) {
			item.m_slowCount.incrementAndGet();
		}
	}

	private static void buildMetricMessage(Map<String, MetricItem> items) {
		Transaction transaction = Cat.newTransaction("System", "UploadMetric" + System.currentTimeMillis());

		for (Entry<String, MetricItem> entry : items.entrySet()) {
			String name = entry.getKey();
			MetricItem item = entry.getValue();
			int count = item.getCount().get();
			long sum = entry.getValue().getSum().get();
			int slowCount = item.getSlowCount();

			if (sum > 0) {
				logMetric(name, "S,C", String.format("%s,%s", count, sum));
			} else if (count > 0) {
				if (item.isLatested()) {
					logMetric(name, "L", String.valueOf(count));
				} else {
					logMetric(name, "C", String.valueOf(count));
				}
			}

			if (slowCount > 0) {
				logMetric(name + ".slowCount", "C", String.valueOf(item.getSlowCount()));
			}
		}
		
		Cat.getManager().getThreadLocalMessageTree().setDiscard(false);

		transaction.setStatus(Transaction.SUCCESS);
		transaction.complete();
	}

	private static MetricItem createMetricItem(String key) {
		MetricItem item = new MetricItem().setKey(key);
		Integer threshold = s_metricThresholds.get(key);

		if (threshold != null) {
			item.setSlowThreshold(threshold);
		}
		return item;
	}

	private static Map<String, MetricItem> getAndResetMetrics() {
		Map<String, MetricItem> cloned = s_metrics;

		synchronized (MetricAggregator.class) {
			s_metrics = new ConcurrentHashMap<String, MetricItem>();
			for (Map.Entry<String, MetricItem> entry : cloned.entrySet()) {
				final String key = entry.getKey();
				s_metrics.put(key, createMetricItem(key));
			}
		}
		return cloned;
	}

	private static void logMetric(String name, String status, String keyValuePairs) {
		try {
			Cat.getProducer().logMetric(name, status, keyValuePairs);
		} catch (Exception e) {
			// ingnore
		}
	}

	public static void logMetricForLatestValue(String name, int quantity) {
		final MetricItem item = makeSureMetricExist(name);

		item.m_count.set(quantity);
		item.setLatested(true);
	}

	public static MetricItem makeSureMetricExist(String key) {
		MetricItem item = s_metrics.get(key);

		if (null == item) {
			synchronized (MetricAggregator.class) {
				item = s_metrics.get(key);
				if (null == item) {
					item = createMetricItem(key);

					s_metrics.put(key, item);
				}
			}
		}
		return item;
	}

	public static void sendMetricData() {
		Map<String, MetricItem> items = MetricAggregator.getAndResetMetrics();

		if (items.size() > 0) {
			buildMetricMessage(items);
		}
	}

	public static void setMetricSlowThreshold(String key, int slow) {
		s_metricThresholds.put(key, slow);
	}

	public static class MetricItem {
		private String m_key;

		private int m_slowThreshold;

		private AtomicInteger m_count = new AtomicInteger();

		private AtomicInteger m_slowCount = new AtomicInteger();

		private AtomicLong m_sum = new AtomicLong();

		private boolean m_latested = false;

		public AtomicInteger getCount() {
			return m_count;
		}

		public String getKey() {
			return m_key;
		}

		public int getSlowCount() {
			return m_slowCount.get();
		}

		public int getSlowThreshold() {
			return m_slowThreshold;
		}

		public AtomicLong getSum() {
			return m_sum;
		}

		public boolean isLatested() {
			return m_latested;
		}

		public MetricItem setKey(String key) {
			m_key = key;
			return this;
		}

		public void setLatested(boolean latested) {
			m_latested = latested;
		}

		public void setSlowThreshold(int slowThreshold) {
			m_slowThreshold = slowThreshold;
		}
	}
}
