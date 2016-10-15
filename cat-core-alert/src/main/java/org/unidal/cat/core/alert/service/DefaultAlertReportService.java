package org.unidal.cat.core.alert.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.config.AlertConfiguration;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.DefaultMerger;
import org.unidal.cat.core.alert.model.transform.DefaultNativeParser;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;

@Named(type = AlertReportService.class)
public class DefaultAlertReportService implements AlertReportService, Initializable {
   @Inject
   private AlertConfiguration m_configuration;

   private ExecutorService m_pool;

   private AlertReport aggregate(Collection<AlertReport> reports) {
      AlertReport aggregated = new AlertReport();

      if (reports.size() > 0) {
         DefaultMerger merger = new DefaultMerger();

         for (AlertReport report : reports) {
            merger.merge(aggregated, report);
         }
      }

      return aggregated;
   }

   private InputStream fetch(Transaction parentTransaction, String server) throws IOException {
      DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
      Transaction t = cat.newTransaction(parentTransaction, "Remote", server);

      try {
         String url = m_configuration.getServerUri(server);
         int ct = m_configuration.getRemoteCallConnectTimeoutInMillis();
         int rt = m_configuration.getRemoteCallReadTimeoutInMillis();

         t.addData(url);

         Map<String, List<String>> headers = new HashMap<String, List<String>>(2);
         InputStream in = Urls.forIO().connectTimeout(ct).readTimeout(rt)//
               .header("Accept-Encoding", "gzip").openStream(url, headers);

         if ("[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
            in = new GZIPInputStream(in);
         }

         t.setStatus(Message.SUCCESS);
         return in;
      } catch (IOException e) {
         t.setStatus(e);
         throw e;
      } catch (RuntimeException e) {
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }

   @Override
   public AlertReport getReport() {
      final Transaction t = Cat.getProducer().newTransaction("Service", "AlertReport");

      try {
         Map<String, Boolean> servers = m_configuration.getServers();
         int len = servers.size();
         List<Callable<AlertReport>> callables = new ArrayList<Callable<AlertReport>>(servers.size());

         for (Map.Entry<String, Boolean> e : servers.entrySet()) {
            if (e.getValue().booleanValue()) {
               final String server = e.getKey();

               callables.add(new Callable<AlertReport>() {
                  @Override
                  public AlertReport call() throws Exception {
                     InputStream in = fetch(t, server);
                     byte[] data = Files.forIO().readFrom(in);
                     AlertReport report = DefaultNativeParser.parse(data);

                     return report;
                  }
               });
            }
         }

         List<AlertReport> reports = new ArrayList<AlertReport>(len);
         int timeout = m_configuration.getRemoteCallReadTimeoutInMillis();

         try {
            List<Future<AlertReport>> futures = m_pool.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);

            for (Future<AlertReport> future : futures) {
               if (future.isDone()) {
                  try {
                     AlertReport report = future.get();

                     if (report != null) {
                        reports.add(report);
                     }
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                     break;
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } else {
                  System.out.println("Future is not completed on timeout. " + future);
               }
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         if (reports.isEmpty()) {
            t.setStatus(Message.SUCCESS);
            return null;
         } else {
            t.addData("reports", reports.size());
            t.setStatus(Message.SUCCESS);

            AlertReport report = aggregate(reports);

            return report;
         }
      } catch (RuntimeException e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }

   @Override
   public void initialize() throws InitializationException {
      int threads = m_configuration.getRemoteCallThreads();

      m_pool = Threads.forPool().getFixedThreadPool("cat-alert", threads);
   }
}
