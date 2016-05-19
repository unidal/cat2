package org.unidal.cat.spi.remote;

import com.dianping.cat.Constants;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Named(type = RemoteSkeleton.class)
public class DefaultRemoteSkeleton extends ContainerHolder implements RemoteSkeleton {
    @Inject
    private ReportManagerManager m_rmm;

    @Inject
    private ReportDelegateManager m_rdg;

    @Override
    public boolean handleReport(RemoteContext ctx, OutputStream out) throws IOException {
        if (ctx.getDomain().equals(Constants.ALL)) {
            return handleAllReport(ctx, out);
        } else {
            return handleNormalReport(ctx, out);
        }
    }

    private boolean handleNormalReport(RemoteContext ctx, OutputStream out) throws IOException {
        String id = ctx.getName();
        ReportManager<Report> rm = m_rmm.getReportManager(id);
        ReportDelegate<Report> delegate = m_rdg.getDelegate(id);
        ReportFilter<Report> filter = ctx.getFilter();

        // find local reports
        List<Report> reports = rm.getLocalReports(ctx.getPeriod(), ctx.getStartTime(), ctx.getDomain());

        if (reports == null || reports.isEmpty()) {
            return false;
        }

        // screen the reports
        List<Report> screenedReports = new ArrayList<Report>();

        for (Report report : reports) {
            Report screenedReport = filter == null ? report : filter.screen(ctx, report);

            if (screenedReport != null) {
                screenedReports.add(screenedReport);
            }
        }

        // aggregate the reports
        Report report = delegate.aggregate(ctx.getPeriod(), screenedReports);

        // tailor it if necessary
        if (filter != null) {
            filter.tailor(ctx, report);
        }

        // write out
        delegate.writeStream(out, report);

        return true;
    }

    private boolean handleAllReport(RemoteContext ctx, OutputStream out) throws IOException {
        String id = ctx.getName();
        ReportManager<Report> rm = m_rmm.getReportManager(id);
        ReportDelegate<Report> delegate = m_rdg.getDelegate(id);
        ReportFilter<Report> filter = ctx.getFilter();

        //find all reports in memory
        int hour = (int) TimeUnit.MILLISECONDS.toHours(ctx.getStartTime().getTime());
        List<Map<String, Report>> reportMapList = rm.getLocalReports(ReportPeriod.HOUR, hour);
        if (reportMapList.size() > 0) {
            List<Report> reportList = new ArrayList<Report>();

            for (Map<String, Report> map : reportMapList) {
                for (Report report : map.values()) {
                    //filter report
                    Report screenedReport = filter == null ? report : filter.screen(ctx, report);
                    reportList.add(screenedReport);
                }
            }

            //make all report
            Report allReport = delegate.makeAllReport(ReportPeriod.HOUR, reportList);

            // tailor it if necessary
            if (filter != null) {
                filter.tailor(ctx, allReport);
            }

            // write out
            delegate.writeStream(out, allReport);

            return true;
        } else {
            return handleNormalReport(ctx, out);
        }
    }
}
