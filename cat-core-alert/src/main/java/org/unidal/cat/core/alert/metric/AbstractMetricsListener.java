package org.unidal.cat.core.alert.metric;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.data.entity.AlertDataShard;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluator;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluatorManager;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractMetricsListener<T extends Metrics> implements MetricsListener<T>, Task, Initializable,
      RoleHintEnabled {
   @Inject
   private MetricsQueue<T> m_queue;

   @Inject
   private RuleEvaluatorManager m_manager;

   private String m_type;

   private List<RuleEvaluator> m_evaluators;

   private AlertDataStore m_store;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public void checkpoint() {
      m_queue.add(newEmptyMetrics());
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

   protected T newEmptyMetrics() {
      Class<T> type = getType();

      try {
         Constructor<T> constructor = type.getDeclaredConstructor(AlertMetric.class);

         constructor.setAccessible(true);
         return constructor.newInstance((AlertMetric) null);
      } catch (Exception e) {
         throw new IllegalStateException(String.format("Constructor(%s) of %s is not found!",
               AlertMetric.class.getSimpleName(), type), e);
      }
   }

   @Override
   public void onMetrics(T metrics) {
      m_queue.add(metrics);
   }

   @Override
   public void run() {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);

      try {
         while (m_enabled.get() || m_queue.size() > 0) {
            T metrics = m_queue.poll();

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

   protected void storeMetrics(AlertDataStore store, T m) {
      String id = getSegmentId(m);
      AlertDataSegment segment = store.findOrCreateSegment(id);
      AlertDataShard shard = segment.findOrCreateShard(m.getFromIp());

      shard.addMetrics(m);
   }
}
