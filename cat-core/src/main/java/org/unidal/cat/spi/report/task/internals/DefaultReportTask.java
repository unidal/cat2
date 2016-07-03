package org.unidal.cat.spi.report.task.internals;

import java.util.Date;
import java.util.List;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.task.ReportTask;

public class DefaultReportTask implements ReportTask {
	private String m_reportName;

	private List<String> m_domains;

	private ReportPeriod m_sourcePeriod;

	private Date m_sourceStartTime;

	private ReportPeriod m_targetPeriod;

	@Override
	public void done(String domain) {
	}

	@Override
	public List<String> getDomains() {
		return m_domains;
	}

	@Override
	public String getReportName() {
		return m_reportName;
	}

	@Override
	public ReportPeriod getSourcePeriod() {
		return m_sourcePeriod;
	}

	@Override
	public Date getSourceStartTime() {
		return m_sourceStartTime;
	}

	@Override
	public ReportPeriod getTargetPeriod() {
		return m_targetPeriod;
	}
}
