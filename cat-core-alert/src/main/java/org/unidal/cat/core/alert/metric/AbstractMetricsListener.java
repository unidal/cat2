package org.unidal.cat.core.alert.metric;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.data.entity.AlertDataShard;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.rule.RuleEvaluator;
import org.unidal.cat.core.alert.rule.RuleEvaluatorManager;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class AbstractMetricsListener<T extends Metrics> implements MetricsListener<T>, Task, Initializable,
      RoleHintEnabled {
   @Inject
   private MetricsQueue<Metrics> m_queue;

   @Inject
   private RuleEvaluatorManager m_manager;

   private String m_type;

   private List<RuleEvaluator> m_evaluators;

   private AlertDataStore m_store;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public void checkpoint() {
      m_queue.add(new CheckpointMetrics(getName()));
   }

   @Override
   public void enableRoleHint(String roleHint) {
      m_type = roleHint;
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   protected abstract String getSegmentId(T m);

   @Override
   public void initialize() throws InitializationException {
      m_evaluators = m_manager.getEvaluators(m_type);
      m_store = m_manager.getStore(m_type);
   }

   @Override
   public void onMetrics(T metrics) {
      m_queue.add(metrics);
   }

   @Override
   @SuppressWarnings("unchecked")
   public void run() {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);

      try {
         while (m_enabled.get() || m_queue.size() > 0) {
            Metrics metrics = m_queue.poll();

            if (metrics != null) {
               if (metrics instanceof CheckpointMetrics) {
                  Transaction t = ((CheckpointMetrics) metrics).fork();

                  try {
                     for (RuleEvaluator evaluator : m_evaluators) {
                        evaluator.evaluate();
                     }

                     t.setStatus(Message.SUCCESS);
                  } finally {
                     t.complete();
                  }
               } else {
                  storeMetrics(m_store, (T) metrics);
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

   protected void storeMetrics(AlertDataStore store, T m) {
      String id = getSegmentId(m);
      AlertDataSegment segment = store.findOrCreateSegment(id);
      AlertDataShard shard = segment.findOrCreateShard(m.getFromIp());

      shard.addMetrics(m);
   }

   static class CheckpointMetrics extends AbstractMetrics {
      private ForkedTransaction m_parent;

      public CheckpointMetrics(String name) {
         super(null);

         m_parent = Cat.newForkedTransaction(AlertConstants.TYPE_ALERT, name);
      }

      public Transaction fork() {
         m_parent.fork();
         return m_parent;
      }
   }
}
