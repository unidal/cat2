package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.metric.Metrics;

public interface TimeWindow<T extends Metrics> {
   public void addMetrics(T metrics);

   public int getLength();

   public double getValue(int index);

   public void reset();

   public void shift();
}