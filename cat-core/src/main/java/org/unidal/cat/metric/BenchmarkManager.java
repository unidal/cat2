package org.unidal.cat.metric;

public interface BenchmarkManager {
	public Benchmark get(String type);
	
	public void remove(String type);
	
	public boolean isEnabled();

	public void setEnabled(boolean enabled);
}
