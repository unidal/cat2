package org.unidal.cat.spi.report.task;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportConstants;

public enum ReportTaskType {
	DAILY(1, ReportConstants.DAILY, ReportPeriod.DAY),

	WEEKLY(2, ReportConstants.WEEKLY, ReportPeriod.WEEK),

	MONTHLY(3, ReportConstants.MONTHLY, ReportPeriod.MONTH),

	YEARLY(4, ReportConstants.YEARLY, ReportPeriod.YEAR);

	private int m_id;

	private String m_name;

	private ReportPeriod m_period;

	private ReportTaskType(int id, String name, ReportPeriod period) {
		m_id = id;
		m_name = name;
		m_period = period;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public ReportPeriod getPeriod() {
		return m_period;
	}
}
