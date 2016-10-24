package org.unidal.cat.core.alert.metric;

public interface MetricsDispatcher {
   public void checkpoint();

   public void dispatch(Metrics metrics);
}
