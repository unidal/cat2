package org.unidal.cat.plugin.transaction.alert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.core.alert.metric.MetricsListener;
import org.unidal.cat.core.alert.metric.MetricsQueue;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = MetricsListener.class, value = "transaction")
public class TransactionMetricsListener implements MetricsListener<TransactionMetrics>, Task {
   @Inject
   private MetricsQueue<TransactionMetrics> m_queue;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public Class<TransactionMetrics> getType() {
      return TransactionMetrics.class;
   }

   @Override
   public void onMetrics(TransactionMetrics metrics) {
      m_queue.add(metrics);
   }

   @Override
   public void run() {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);

      try {
         while (m_enabled.get()) {
            TransactionMetrics metrics = m_queue.poll();

            if (metrics != null) {
               // TODO
            }
         }
      } catch (InterruptedException e) {
         // ignore
      }

      m_latch.countDown();
   }

   @Override
   public void shutdown() {
      m_enabled.set(false);

      try {
         m_latch.await();
      } catch (InterruptedException e) {
         // ignore it
      }
   }

   static class Condition {

   }

   static enum Fields {
      HITS,

      FAILURES,

      DURATION,

      FAILURE_RATE;
   }

   static enum Functions {
      NOW,

      SUM,

      AVG,

      MIN,

      MAX;
   }
}
