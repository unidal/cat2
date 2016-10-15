package org.unidal.cat.core.alert.metric;

public interface MetricsListener<T extends Metrics> {
   public Class<T> getType();

   public void onMetrics(T metrics);
}
