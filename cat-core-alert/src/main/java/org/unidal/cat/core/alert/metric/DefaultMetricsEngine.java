package org.unidal.cat.core.alert.metric;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.config.AlertConfiguration;
import org.unidal.cat.core.alert.model.AlertReportService;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.BaseVisitor;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = MetricsEngine.class)
public class DefaultMetricsEngine extends ContainerHolder implements MetricsEngine, Initializable {
   @Inject
   private AlertConfiguration m_config;

   @Inject
   private AlertReportService m_service;

   @Inject
   private MetricsDispatcher m_dispatcher;

   private Feeder m_feeder;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public void initialize() throws InitializationException {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);
      m_feeder = new Feeder();
   }

   @Override
   public void run() {
      long interval = m_config.getAlertCheckInterval();

      try {
         TimeUnit.SECONDS.sleep(30);

         while (m_enabled.get()) {
            Transaction t = Cat.newTransaction(AlertConstants.TYPE_ALERT, "Check");
            long start = System.currentTimeMillis();

            try {
               AlertReport report = m_service.getReport();

               if (report != null) {
                  report.accept(m_feeder);
               }

               t.setStatus(Message.SUCCESS);
            } catch (Throwable e) {
               t.setStatus(e);
               Cat.logError(e);
            } finally {
               t.complete();
            }

            while (m_enabled.get()) {
               long remain = start + interval - System.currentTimeMillis();

               if (remain <= 0) {
                  break;
               } else if (remain > 1000) {
                  TimeUnit.MILLISECONDS.sleep(1000);
               } else {
                  TimeUnit.MILLISECONDS.sleep(remain);
               }
            }
         }
      } catch (InterruptedException e) {
         // ignore it
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

   private class Feeder extends BaseVisitor {
      private String m_fromIp;

      private String m_typeName;

      private String m_typeClass;

      @Override
      public void visitEvent(AlertEvent event) {
         m_typeName = event.getTypeName();
         m_typeClass = event.getTypeClass();
         super.visitEvent(event);
      }

      @Override
      public void visitMachine(AlertMachine machine) {
         m_fromIp = machine.getIp();
         super.visitMachine(machine);
      }

      @Override
      public void visitMetric(AlertMetric metric) {
         metric.setFromIp(m_fromIp);
         metric.setTypeName(m_typeName);
         metric.setTypeClass(m_typeClass);

         m_dispatcher.dispatch(metric.getMetrics());
      }

      @Override
      public void visitAlertReport(AlertReport alertReport) {
         super.visitAlertReport(alertReport);

         m_dispatcher.checkpoint();
      }
   }
}
