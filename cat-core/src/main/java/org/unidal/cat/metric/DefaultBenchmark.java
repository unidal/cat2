package org.unidal.cat.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultBenchmark implements Benchmark {
	static final long MICROSECOND = 1000L;

	private AtomicBoolean m_enabled;

	private String m_type;

	private long m_startTime;

	private long m_endTime;

	private ConcurrentMap<String, Metric> m_metrics = new ConcurrentHashMap<String, Metric>();

	DefaultBenchmark(String type, AtomicBoolean enabled) {
		m_type = type;
		m_enabled = enabled;
		m_startTime = System.nanoTime();
	}

	@Override
	public Metric begin(String name) {
		Metric metric = get(name);

		metric.start();
		return metric;
	}

	@Override
	public Metric end(String name) {
		Metric metric = get(name);

		metric.end();
		return metric;
	}

	@Override
	public Metric get(String name) {
		Metric metric = m_metrics.get(name);

		if (metric == null) {
			Metric m = new DefaultMetric(name);

			if ((metric = m_metrics.putIfAbsent(name, m)) == null) {
				metric = m;
			}
		}

		return metric;
	}

	@Override
	public String getType() {
		return m_type;
	}

	@Override
	public void print() {
		// TODO
		if (m_enabled.get()) {
			m_endTime = System.nanoTime();

			System.out.println(this);
		}
	}

	@Override
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