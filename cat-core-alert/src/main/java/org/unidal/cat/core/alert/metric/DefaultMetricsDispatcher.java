package org.unidal.cat.core.alert.metric;

import org.unidal.lookup.annotation.Named;

@Named(type = MetricsDispatcher.class)
public class DefaultMetricsDispatcher implements MetricsDispatcher {
   @Override
   public void dispatch(Metrics metrics) {
      System.out.println(metrics);
   }
}
