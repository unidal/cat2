package org.unidal.cat.spi.report.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.CatConstant;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.core.report.remote.RemoteReportStub;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportConfiguration;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = ReportProvider.class, value = RecentReportProvider.ID)
public class RecentReportProvider<T extends Report> implements ReportProvider<T>, Initializable {
   public static final String ID = "recent";

   @Inject
   private RemoteReportStub m_stub;

   @Inject
   private ReportConfiguration m_configuration;

   private ExecutorService m_pool;

   @Override
   public boolean isEligible(RemoteReportContext ctx, ReportDelegate<T> delegate) {
      return !ctx.getPeriod().isHistorical(ctx.getStartTime());
   }

   @Override
   public T getReport(final RemoteReportContext ctx, final ReportDelegate<T> delegate) {
      final Transaction t = Cat.getProducer().newTransaction("Service", "Recent");

      try {
         Map<String, Boolean> servers = m_configuration.getServers();
         int len = servers.size();
         List<Callable<T>> callables = new ArrayList<Callable<T>>(servers.size());

         for (Map.Entry<String, Boolean> e : servers.entrySet()) {
            if (e.getValue().booleanValue()) {
               final String server = e.getKey();

               callables.add(new Callable<T>() {
                  @Override
                  public T call() throws Exception {
                     ctx.setParentTransaction(t);

                     InputStream in = m_stub.getReport(ctx, server);
                     T report = delegate.readStream(in);

                     return report;
                  }
               });
            }
         }

         List<T> reports = new ArrayList<T>(len);
         int timeout = m_configuration.getRemoteCallReadTimeoutInMillis();

         try {
            List<Future<T>> futures = m_pool.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);

            for (Future<T> future : futures) {
               if (future.isDone()) {
                  try {
                     T report = future.get();

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
            T report = delegate.aggregate(ctx.getPeriod(), reports);
            ReportFilter<Report> filter = ctx.getFilter();

            if (filter != null) {
               filter.tailor(ctx, report);
            }

            t.setStatus(Message.SUCCESS);
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

      m_pool = Threads.forPool().getFixedThreadPool(CatConstant.CAT + "-ReportService", threads);
   }
}
