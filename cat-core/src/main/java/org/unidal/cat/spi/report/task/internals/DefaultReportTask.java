package org.unidal.cat.spi.report.task.internals;

import java.util.Date;

import org.unidal.cat.dal.report.ReportTaskDo;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.task.ReportTask;

public class DefaultReportTask implements ReportTask {
	private ReportTaskDo m_task;

	public DefaultReportTask(ReportTaskDo task) {
		m_task = task;
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
		ReportPeriod period = getTargetPeriod();

		switch (period) {
		case DAY:
			return ReportPeriod.HOUR;
		case WEEK:
			return ReportPeriod.DAY;
		case MONTH:
			return ReportPeriod.DAY;
		case YEAR:
			return ReportPeriod.MONTH;
		default:
			throw new IllegalStateException(String.format("Invalid target period(%s) of task(%s)!", period, m_task));
		}
	}

	@Override
	public ReportPeriod getTargetPeriod() {
		ReportPeriod period = ReportPeriod.getById(m_task.getTaskType(), null);

		if (period != null) {
			return period;
		} else {
			throw new IllegalStateException(String.format("Invalid type(%s) of task(%s)!", m_task.getTaskType(), m_task));
		}
	}

	@Override
	public Date getTargetStartTime() {
		return m_task.getReportStartTime();
	}

	@Override
	public String toString() {
		return m_task.toString();
	}
}
