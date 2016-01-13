package org.unidal.cat.report.spi.internals;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportManagerManager;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.ReportDelegateManager;
import org.unidal.cat.report.spi.ReportManager;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.cat.report.spi.remote.RemoteSkeleton;
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
	public boolean handleReport(RemoteContext ctx, OutputStream out) throws IOException {
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
}
