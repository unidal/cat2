package org.unidal.cat.metric;

import java.util.concurrent.TimeUnit;

public class Metric {
	private String m_name;

	private long m_sum; // sum of duration

	private long m_sum2; // sum of square of duration

	private long m_count; // count of duration

	private long m_start = -1; // start time of duration

	public Metric(String name) {
		m_name = name;
	}

	public void start() {
		m_start = System.nanoTime();
	}

	public void end() {
		if (m_start == -1) {
			throw new IllegalStateException(String.format("Watcher(%s) is not begin yet", m_name));
		} else {
			long durationInMicro = (System.nanoTime() - m_start) / Benchmark.MICROSECOND;

			m_count++;
			m_sum += durationInMicro;
			m_sum2 += durationInMicro * durationInMicro;
			m_start = -1;
		}
	}

	public long getCount() {
		return m_count;
	}

	public String getName() {
		return m_name;
	}

	public long getSum() {
		return m_sum;
	}

	public long getSum2() {
		return m_sum2;
	}

	private String getUnitName(TimeUnit unit) {
		if (TimeUnit.MILLISECONDS == unit) {
			return "ms";
		} else if (TimeUnit.MICROSECONDS == unit) {
			return "us";
		} else {
			return unit.name().toLowerCase();
		}
	}

	@Override
	public String toString() {
		return toString(TimeUnit.MILLISECONDS, 0);
	}

	public String toString(TimeUnit unit, long durationInMicro) {
		long sum = unit.convert(m_sum, TimeUnit.MICROSECONDS);
		long duration = unit.convert(durationInMicro, TimeUnit.MICROSECONDS);
		String un = getUnitName(unit);
		StringBuilder sb = new StringBuilder(1024);

		sb.append(m_name);
		sb.append(": count=").append(m_count);
		sb.append(", sum=").append(sum).append(un);
		sb.append(", avg=").append(String.format("%.2f", 1.0 * sum / m_count)).append(un);
		sb.append(", ratio=").append(String.format("%.2f", 100.0 * sum / duration)).append('%');

		return sb.toString();
	}
}