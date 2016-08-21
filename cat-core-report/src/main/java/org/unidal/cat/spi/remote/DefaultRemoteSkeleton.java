package org.unidal.cat.spi.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.ReportManagerManager;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = RemoteSkeleton.class)
public class DefaultRemoteSkeleton extends ContainerHolder implements RemoteSkeleton {
   @Inject
   private ReportManagerManager m_rmm;

   @Inject
   private ReportDelegateManager m_rdg;

   @Override
   public void handleReport(RemoteContext ctx, OutputStream out) throws IOException {
      String name = ctx.getName();
      ReportManager<Report> rm = m_rmm.getReportManager(name);

      // find local reports
      List<Report> reports = rm.getReports(ctx.getPeriod(), ctx.getStartTime(), ctx.getDomain(), ctx.getProperties());

      if (reports == null || reports.isEmpty()) {
         return;
      }

      // screen the reports
      ReportFilter<Report> filter = ctx.getFilter();
      List<Report> screenedReports = new ArrayList<Report>();

      for (Report report : reports) {
         Report screenedReport = filter == null ? report : filter.screen(ctx, report);

         if (screenedReport != null) {
            screenedReports.add(screenedReport);
         }
      }

      if (screenedReports.size() > 0) {
         // aggregate the reports
         ReportDelegate<Report> delegate = m_rdg.getDelegate(name);
         Report report = delegate.aggregate(ctx.getPeriod(), screenedReports);

         // tailor it if necessary
         if (filter != null) {
            filter.tailor(ctx, report);
         }

         // write out
         delegate.writeStream(out, report);
      }
   }
}
