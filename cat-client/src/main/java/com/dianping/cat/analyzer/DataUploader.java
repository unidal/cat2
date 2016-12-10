package com.dianping.cat.analyzer;

import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named
public class DataUploader implements Task {
   @Inject
   private ClientConfigurationManager m_configManager;

   private boolean m_active = true;

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public void run() {
      while (m_active && m_configManager.getConfig().isEnabled()) {
         long start = System.currentTimeMillis();

         try {
            MetricAggregator.sendMetricData();
            TransactionAggregator.sendTransactionData();
            EventAggregator.sendEventData();
         } catch (Exception ex) {
            Cat.logError(ex);
         }

         long duration = System.currentTimeMillis() - start;

         if (duration < 1000) {
            try {
               Thread.sleep(1000 - duration);
            } catch (InterruptedException e) {
               break;
            }
         }
      }
   }

   @Override
   public void shutdown() {
      m_active = false;
   }
}
