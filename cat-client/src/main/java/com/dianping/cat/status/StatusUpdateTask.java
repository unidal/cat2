package com.dianping.cat.status;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.Cat;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.datasource.c3p0.C3P0InfoCollector;
import com.dianping.cat.status.datasource.druid.DruidInfoCollector;
import com.dianping.cat.status.jvm.ClassLoadingInfoCollector;
import com.dianping.cat.status.jvm.JvmInfoCollector;
import com.dianping.cat.status.jvm.ThreadInfoCollector;
import com.dianping.cat.status.model.entity.CustomInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.send.HttpAgentSender;
import com.dianping.cat.status.send.HttpSendConfig;
import com.dianping.cat.status.system.ProcessorInfoCollector;
import com.dianping.cat.status.system.StaticInfoCollector;

@Named
public class StatusUpdateTask implements Task, Initializable {
   @Inject
   private MessageStatistics m_statistics;

   @Inject
   private ClientConfigurationManager m_configManager;

   private boolean m_active = true;

   private long m_interval = 60 * 1000;

   private HttpAgentSender m_sender;

   private boolean m_httpEnabled;

   private void buildExtraStatus(StatusInfo status) {
      StatusExtensionRegister res = StatusExtensionRegister.getInstance();
      List<StatusExtension> extensions = res.getStatusExtension();

      for (StatusExtension extension : extensions) {
         Transaction t = Cat.newTransaction("System", "StatusExtension-" + extension.getId());

         try {
            Map<String, String> propertis = extension.getProperties();

            if (propertis.size() > 0) {
               String id = extension.getId();
               String des = extension.getDescription();
               Extension item = status.findOrCreateExtension(id).setDescription(des);
               long mills = System.currentTimeMillis();

               for (Entry<String, String> entry : propertis.entrySet()) {
                  final String key = entry.getKey();
                  final String value = entry.getValue();

                  try {
                     double doubleValue = Double.parseDouble(value);

                     item.findOrCreateExtensionDetail(key).setValue(doubleValue);

                     if (m_httpEnabled) {
                        m_sender.asyncSend(key, value, mills);
                     }
                  } catch (Exception e) {
                     status.getCustomInfos().put(key, new CustomInfo().setKey(key).setValue(value));
                  }
               }
            }
            t.setSuccessStatus();
         } catch (Exception e) {
            t.setStatus(e);
         } finally {
            t.complete();
         }
      }
   }

   private String buildJstack() {
      ThreadMXBean bean = ManagementFactory.getThreadMXBean();

      bean.setThreadContentionMonitoringEnabled(true);

      ThreadInfo[] threads = bean.dumpAllThreads(false, false);

      return threadDump(threads);
   }

   @Override
   public String getName() {
      return "StatusUpdateTask";
   }

   @Override
   public void initialize() throws InitializationException {
      try {
         JvmInfoCollector.getInstance().registerJVMCollector();
         StatusExtensionRegister.getInstance().register(new StaticInfoCollector());
         StatusExtensionRegister.getInstance().register(new ClassLoadingInfoCollector());
         StatusExtensionRegister.getInstance().register(new ProcessorInfoCollector());
         StatusExtensionRegister.getInstance().register(new ThreadInfoCollector());
         StatusExtensionRegister.getInstance().register(new C3P0InfoCollector());
         StatusExtensionRegister.getInstance().register(new DruidInfoCollector());
         HttpSendConfig config = HttpSendConfig.loadDefaultConfig();

         m_httpEnabled = (!config.isDisabled()) && (!jmonitorExsit());

         if (m_httpEnabled) {
            m_sender = HttpAgentSender.getInstance(config);

            m_sender.initWorkThread();
         }
      } catch (Exception e) {
         // ignore
      }
   }

   private boolean jmonitorExsit() {
      try {
         Class.forName("com.meituan.jmonitor.config.JMonitorConfig");

         return true;
      } catch (Exception e) {
         return false;
      }
   }

   @Override
   public void run() {
      // try to wait cat client init success
      try {
         Thread.sleep(10 * 1000);
      } catch (InterruptedException e) {
         return;
      }

      while (true) {
         Calendar cal = Calendar.getInstance();
         int second = cal.get(Calendar.SECOND);

         // try to avoid send heartbeat at 59-01 second
         if (second < 2 || second > 58) {
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
               // ignore it
            }
         } else {
            break;
         }
      }

      Transaction reboot = Cat.newTransaction("System", "Reboot");
      final String localHostAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

      reboot.setStatus(Message.SUCCESS);
      Cat.logEvent("Reboot", localHostAddress);
      reboot.complete();

      while (m_active && Cat.isEnabled()) {
         long start = MilliSecondTimer.currentTimeMillis();

         if (m_configManager.getConfig().isEnabled()) {
            Transaction t = Cat.newTransaction("System", "Status");
            Heartbeat h = Cat.CAT2.getProducer().newHeartbeat("Heartbeat", localHostAddress);
            StatusInfo status = new StatusInfo();

            try {
               buildExtraStatus(status);
               h.addData(status.toString());
               h.setStatus(Message.SUCCESS);
            } catch (Throwable e) {
               h.setStatus(e);
               Cat.logError(e);
            } finally {
               h.complete();
            }
            Cat.CAT2.getProducer().logEvent("Heartbeat", "jstack", Event.SUCCESS, buildJstack());
            t.setStatus(Message.SUCCESS);
            t.complete();
         }

         try {
            long current = System.currentTimeMillis() / 1000 / 60;
            int min = (int) (current % (60));

            // refresh config 3 minute
            if (min % 3 == 0) {
               // m_configManager.refreshConfig(); TODO remove it?
            }
         } catch (Exception e) {
            // ignore
         }

         long elapsed = MilliSecondTimer.currentTimeMillis() - start;

         if (elapsed < m_interval) {
            try {
               Thread.sleep(m_interval - elapsed);
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

   private String threadDump(ThreadInfo[] threads) {
      StringBuilder sb = new StringBuilder(32768);
      int index = 1;

      TreeMap<String, ThreadInfo> sortedThreads = new TreeMap<String, ThreadInfo>();

      for (ThreadInfo thread : threads) {
         sortedThreads.put(thread.getThreadName(), thread);
      }

      for (ThreadInfo thread : sortedThreads.values()) {
         sb.append(index++).append(": ").append(thread);
      }

      return sb.toString();
   }
}
