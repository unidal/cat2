package org.unidal.cat.spi.report.task.internals;

import java.util.Date;
import java.util.List;

import org.unidal.cat.dal.report.ReportTaskDo;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.task.ReportTask;

public class DefaultReportTask implements ReportTask {
	private List<String> m_domains;

	private ReportPeriod m_sourcePeriod;

	private ReportPeriod m_targetPeriod;

	private ReportTaskDo m_task;

	public DefaultReportTask(ReportTaskDo task) {
		m_task = task;
	}

	@Override
	public void done(String domain) {
	}

	@Override
	public List<String> getDomains() {
		return m_domains;
	}

	@Override
	public int getFailureCount() {
		return m_task.getFailureCount();
	}

	@Override
	public int getId() {
		return m_task.getId();
	}

	@Override
	public String getReportName() {
		return m_task.getReportName();
	}

	@Override
	public ReportPeriod getSourcePeriod() {
		return m_sourcePeriod;
	}

	@Override
	public Date getSourceStartTime() {
		return m_task.getReportStartTime();
	}

	@Override
	public ReportPeriod getTargetPeriod() {
		return m_targetPeriod;
	}
}
