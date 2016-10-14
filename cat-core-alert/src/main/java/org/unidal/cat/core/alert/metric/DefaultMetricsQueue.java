package org.unidal.cat.core.alert.metric;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.unidal.lookup.annotation.Named;

@Named(type = MetricsQueue.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMetricsQueue<T extends Metrics> implements MetricsQueue<T> {
   private BlockingQueue<T> m_queue = new LinkedBlockingQueue<T>(5000);

   @Override
   public void add(T metrics) {
      m_queue.add(metrics);
   }
}
