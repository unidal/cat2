package org.unidal.cat.core.alert.metric;

public interface MetricsListener<T extends Metrics> {
   public void onMetrics(T metrics);
}
