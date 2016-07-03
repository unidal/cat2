package org.unidal.cat.spi.report.task.internals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.cat.spi.report.ReportReducerManager;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.cat.spi.report.task.ReportTaskExecutor;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportTaskExecutor.class)
public class DefaultReportTaskExecutor implements ReportTaskExecutor {
	@Inject
	private ReportReducerManager m_reducerManager;

	@Inject
	private ReportStorage<Report> m_storage;

	@Inject
	private ReportDelegateManager m_delegateManager;

	@Override
	public void execute(ReportTask task) throws IOException {
		String reportName = task.getReportName();
		ReportPeriod sourcePeriod = task.getSourcePeriod();
		ReportPeriod targetPeriod = task.getTargetPeriod();
		Date sourceStartTime = task.getSourceStartTime();
		List<String> domains = task.getDomains();
		ReportReducer<Report> reducer = m_reducerManager.getReducer(reportName, targetPeriod.getName());
		ReportDelegate<Report> delegate = m_delegateManager.getDelegate(reportName);

		for (String domain : domains) {
			List<Report> reports = m_storage.loadAll(delegate, sourcePeriod, sourceStartTime, domain);
			Report report = reducer.reduce(reports);

			m_storage.store(delegate, targetPeriod, report, 0, ReportStoragePolicy.FILE_AND_MYSQL);
			task.done(domain);
		}
	}
}
