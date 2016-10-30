package org.unidal.cat.core.alert.metric;

public interface MetricsQueue<T extends Metrics> {
   public void add(T metrics);

   public T poll() throws InterruptedException;

   public int size();
}
