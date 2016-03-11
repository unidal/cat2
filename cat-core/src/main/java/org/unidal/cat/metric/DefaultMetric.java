package org.unidal.cat.metric;

import java.util.concurrent.TimeUnit;

public class DefaultMetric implements Metric {
	private String m_name;

	private long m_sum; // sum of duration

	private long m_sum2; // sum of square of duration

	private long m_count; // count of duration

	private long m_start = -1; // start time of duration

	public DefaultMetric(String name) {
		m_name = name;
	}

	@Override
	public void end() {
		if (m_start == -1) {
			throw new IllegalStateException(String.format("Metric(%s) is not started yet", m_name));
		} else {
			long durationInMicro = (System.nanoTime() - m_start) / 1000L;

			m_count++;
			m_sum += durationInMicro;
			m_sum2 += durationInMicro * durationInMicro;
			m_start = -1;
		}
	}

	@Override
	public long getCount() {
		return m_count;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public long getSum() {
		return m_sum;
	}

	@Override
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
	public void start() {
		m_start = System.nanoTime();
	}

	@Override
	public String toString() {
		return toString(TimeUnit.MILLISECONDS, 0);
	}

	@Override
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