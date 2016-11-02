package org.unidal.cat.core.alert.metric;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.lookup.annotation.Named;

@Named(type = MetricsQueue.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMetricsQueue<T extends Metrics> implements MetricsQueue<T> {
   private BlockingQueue<T> m_queue = new LinkedBlockingQueue<T>(5000);

   @Override
   public void add(T metrics) {
      m_queue.add(metrics);
   }

   @Override
   public T poll() throws InterruptedException {
      return m_queue.poll(5, TimeUnit.MILLISECONDS);
   }

   @Override
   public int size() {
      return m_queue.size();
   }
}
