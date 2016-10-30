package org.unidal.cat.plugin.transaction.alert;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.data.entity.AlertDataShard;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.metric.MetricsListener;
import org.unidal.cat.core.alert.metric.MetricsQueue;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluator;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluatorManager;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = MetricsListener.class, value = TransactionConstants.NAME)
public class TransactionMetricsListener implements MetricsListener<TransactionMetrics>, Task, Initializable {
   @Inject
   private MetricsQueue<TransactionMetrics> m_queue;

   @Inject
   private RuleEvaluatorManager m_manager;

   private List<RuleEvaluator> m_evaluators;

   private AlertDataStore m_store;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public void checkpoint() {
      m_queue.add(new TransactionMetrics(null));
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public Class<TransactionMetrics> getType() {
      return TransactionMetrics.class;
   }

   @Override
   public void initialize() throws InitializationException {
      m_evaluators = m_manager.getEvaluators(TransactionConstants.NAME);
      m_store = m_manager.getStore(TransactionConstants.NAME);
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
         while (m_enabled.get() || m_queue.size() > 0) {
            TransactionMetrics metrics = m_queue.poll();

            if (metrics != null) {
               if (metrics.getAlertMetric() == null) {
                  for (RuleEvaluator evaluator : m_evaluators) {
                     evaluator.evaluate();
                  }
               } else {
                  storeMetrics(m_store, metrics);
               }
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

   private void storeMetrics(AlertDataStore store, TransactionMetrics m) {
      String id = m.getTypeName() + ":" + m.getDomain() + ":" + m.getType() + ":" + m.getName();
      AlertDataSegment segment = store.findOrCreateSegment(id);
      AlertDataShard shard = segment.findOrCreateShard(m.getFromIp());

      shard.addMetrics(m);
   }
}
