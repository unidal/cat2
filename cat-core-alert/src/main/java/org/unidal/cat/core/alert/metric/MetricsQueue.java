package org.unidal.cat.core.alert.metric;

public interface MetricsQueue<T extends Metrics> {
   public void add(T metrics);
}
