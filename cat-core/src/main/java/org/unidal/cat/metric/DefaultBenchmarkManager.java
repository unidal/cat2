package org.unidal.cat.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.lookup.annotation.Named;

@Named(type = BenchmarkManager.class)
public class DefaultBenchmarkManager implements BenchmarkManager {
	private ConcurrentMap<String, Benchmark> m_benchmarks = new ConcurrentHashMap<String, Benchmark>();

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	@Override
	public Benchmark get(String type) {
		Benchmark b = m_benchmarks.get(type);

		if (b == null) {
			Benchmark bm = new DefaultBenchmark(type, m_enabled);

			if ((b = m_benchmarks.putIfAbsent(type, bm)) == null) {
				b = bm;
			}
		}

		return b;
	}

	@Override
	public boolean isEnabled() {
		return m_enabled.get();
	}

	@Override
	public void setEnabled(boolean enabled) {
		m_enabled.set(enabled);
	}
}
