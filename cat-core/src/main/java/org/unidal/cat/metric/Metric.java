package org.unidal.cat.metric;

import java.util.concurrent.TimeUnit;

public interface Metric {
	public void start();

	public void end();

	public long getCount();

	public String getName();

	public long getSum();

	public long getSum2();

	public String toString(TimeUnit unit, long durationInMicro);
}