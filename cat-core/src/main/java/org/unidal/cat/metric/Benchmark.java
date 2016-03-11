package org.unidal.cat.metric;

public interface Benchmark {
	public Metric begin(String name);

	public Metric end(String name);

	public Metric get(String name);

	public String getType();

	public void print();

	public void reset();
}