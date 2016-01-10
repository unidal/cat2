package org.unidal.cat.report.spi.internals;

import java.io.IOException;
import java.io.OutputStream;
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
	public void handleReport(RemoteContext ctx, OutputStream out) throws IOException {
		String id = ctx.getName();
		ReportManager<Report> rm = m_rmm.getReportManager(id);
		ReportDelegate<Report> delegate = m_rdg.getDelegate(id);
		List<Report> reports = rm.getLocalReports(ctx.getPeriod(), ctx.getStartTime(), ctx.getDomain());

		if (reports == null || reports.isEmpty()) {
			throw new NullPointerException(String.format("No report found by %s", ctx));
		}

		ReportFilter<Report> filter = ctx.getFilter();
		Report report = delegate.aggregate(ctx.getPeriod(), reports);

		if (filter != null) {
			filter.applyTo(ctx, report);
		}

		delegate.writeStream(out, report);
	}
}
