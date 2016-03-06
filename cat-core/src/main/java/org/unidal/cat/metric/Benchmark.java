package org.unidal.cat.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class Benchmark {
	static final long MICROSECOND = 1000L;

	private String m_type;

	private long m_startTime;

	private long m_endTime;

	private ConcurrentMap<String, Metric> m_metrics = new ConcurrentHashMap<String, Metric>();

	public Benchmark(String type) {
		m_type = type;
		m_startTime = System.nanoTime();
	}

	public Metric begin(String name) {
		Metric metric = m_metrics.get(name);

		if (metric == null) {
			Metric m = new Metric(name);

			if ((metric = m_metrics.putIfAbsent(name, m)) == null) {
				metric = m;
			}
		}

		metric.start();
		return metric;
	}

	public Metric end(String name) {
		Metric metric = m_metrics.get(name);

		if (metric == null) {
			Metric w = new Metric(name);

			if ((metric = m_metrics.putIfAbsent(name, w)) == null) {
				metric = w;
			}
		}

		metric.end();
		return metric;
	}

	public Metric get(String name) {
		Metric metric = m_metrics.get(name);

		if (metric == null) {
			Metric m = new Metric(name);

			if ((metric = m_metrics.putIfAbsent(name, m)) == null) {
				metric = m;
			}
		}

		return metric;
	}

	public String getType() {
		return m_type;
	}

	public void print() {
		m_endTime = System.nanoTime();

		System.out.println(this);
	}

	public void reset() {
		m_startTime = System.nanoTime();
		m_metrics.clear();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		long duration;

		if (m_endTime > 0) {
			duration = (m_endTime - m_startTime) / MICROSECOND;
		} else {
			duration = (System.nanoTime() - m_startTime) / MICROSECOND;
		}

		sb.append(m_type).append(":\r\n");

		for (Metric metric : m_metrics.values()) {
			sb.append("   ").append(metric.toString(TimeUnit.MILLISECONDS, duration)).append("\r\n");
		}

		return sb.toString();
	}
}