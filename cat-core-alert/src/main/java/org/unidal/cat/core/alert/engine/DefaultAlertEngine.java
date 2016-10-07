package org.unidal.cat.core.alert.engine;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.AlertConfiguration;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.BaseVisitor;
import org.unidal.cat.core.alert.service.AlertReportService;
import org.unidal.helper.Dates;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

@Named(type = AlertEngine.class)
public class DefaultAlertEngine extends ContainerHolder implements AlertEngine, Initializable {
   @Inject
   private AlertConfiguration m_config;

   @Inject
   private AlertReportService m_service;

   @Inject
   private AlertRegistry m_registry;

   private EPServiceProvider m_esper;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   private Feeder m_feeder;

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public void initialize() throws InitializationException {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);
      m_feeder = new Feeder();

      initializeEsper();
   }

   private void initializeEsper() {
      m_esper = EPServiceProviderManager.getDefaultProvider();

      EPAdministrator admin = m_esper.getEPAdministrator();
      ConfigurationOperations config = admin.getConfiguration();

      Map<String, AlertListener> listeners = lookupMap(AlertListener.class);

      for (Map.Entry<String, AlertListener> e : listeners.entrySet()) {
         config.addEventType(e.getKey(), AlertMetric.class);
      }
   }

   @Override
   public void register(AlertListener listener) {
      m_registry.register(m_esper, listener);
   }

   @Override
   public void run() {
      long interval = m_config.getAlertCheckInterval();

      try {
         while (m_enabled.get()) {
            Transaction t = Cat.newTransaction("Alert", Dates.now().asString("mm"));
            long start = System.currentTimeMillis();

            try {
               AlertReport report = m_service.getReport();

               if (report != null) {
                  report.accept(m_feeder);
               }
            } catch (Throwable e) {
               e.printStackTrace();
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

   private void sendEvent(AlertMetric metric) {
      Object event = m_registry.buildEvent(metric);

      m_esper.getEPRuntime().sendEvent(event);
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
      private String m_type;

      private String m_ip;

      @Override
      public void visitEvent(AlertEvent event) {
         m_type = event.getType();
         super.visitEvent(event);
      }

      @Override
      public void visitMachine(AlertMachine machine) {
         m_ip = machine.getIp();
         super.visitMachine(machine);
      }

      @Override
      public void visitMetric(AlertMetric metric) {
         metric.setType(m_type);
         metric.setIp(m_ip);

         sendEvent(metric);
      }
   }
}
