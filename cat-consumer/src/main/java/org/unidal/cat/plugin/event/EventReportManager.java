package org.unidal.cat.plugin.event;

import org.unidal.cat.report.spi.ReportManager;
import org.unidal.cat.report.spi.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.model.entity.EventReport;

@Named(type = ReportManager.class, value = EventConstants.ID)
public class EventReportManager extends AbstractReportManager<EventReport> {
	@Override
	public int getThreadsCount() {
		return 2;
	}
}
