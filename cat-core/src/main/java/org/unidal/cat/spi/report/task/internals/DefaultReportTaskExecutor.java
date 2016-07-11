package org.unidal.cat.spi.report.task.internals;

import static org.unidal.cat.spi.ReportPeriod.DAY;
import static org.unidal.cat.spi.ReportPeriod.MONTH;
import static org.unidal.cat.spi.ReportPeriod.WEEK;
import static org.unidal.cat.spi.ReportPeriod.YEAR;

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
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.helper.Inets;
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

	@Inject
	private ReportTaskTracerManager m_trackerManager;

	@Inject
	private ReportTaskService m_taskService;

	@Override
	public void execute(ReportTask task) throws Exception {
		String reportName = task.getReportName();
		ReportPeriod sourcePeriod = task.getSourcePeriod();
		ReportPeriod targetPeriod = task.getTargetPeriod();
		ReportReducer<Report> reducer = m_reducerManager.getReducer(reportName, targetPeriod.getName());
		ReportDelegate<Report> delegate = m_delegateManager.getDelegate(reportName);
		ReportTaskTracker tracker = m_trackerManager.open(task);

		for (String domain : tracker.getDomains()) {
			List<Report> reports = m_storage.loadAll(delegate, sourcePeriod, task.getTargetStartTime(), domain);
			Report report = reducer.reduce(reports);

			m_storage.store(delegate, targetPeriod, report, 0, ReportStoragePolicy.FILE_AND_MYSQL);
			tracker.done(domain);
		}

		m_trackerManager.close(tracker);

		produceNextTasks(task);
	}

	private void produceNextTasks(ReportTask task) throws Exception {
		String reportName = task.getReportName();
		Date startTime = task.getTargetStartTime();
		String ip = Inets.IP4.getLocalHostAddress();
		ReportPeriod period = task.getTargetPeriod();

		switch (period) {
		case HOUR:
			m_taskService.add(ip, DAY, DAY.getStartTime(startTime), reportName, DAY.getReduceTime(startTime));
			break;
		case DAY:
			m_taskService.add(ip, WEEK, WEEK.getStartTime(startTime), reportName, WEEK.getReduceTime(startTime));
			m_taskService.add(ip, MONTH, MONTH.getStartTime(startTime), reportName, MONTH.getReduceTime(startTime));
			break;
		case WEEK:
			break;
		case MONTH:
			m_taskService.add(ip, YEAR, YEAR.getStartTime(startTime), reportName, YEAR.getReduceTime(startTime));
			break;
		default:
			break;
		}
	}
}
