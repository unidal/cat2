package org.unidal.cat.plugin.event.reducer;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.helper.Dates;
import org.unidal.lookup.annotation.Named;

import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.plugin.event.model.entity.EventReport;

@Named(type = ReportReducer.class, value = EventConstants.NAME + ":" + EventDailyReducer.ID)
public class EventDailyReducer extends AbstractEventReducer implements ReportReducer<EventReport> {
	public static final String ID = DAILY;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ReportPeriod getPeriod() {
		return ReportPeriod.DAY;
	}

	@Override
	protected int getRangeValue(EventReport report, Range range) {
		int hour = Dates.from(report.getStartTime()).hour();

		return hour;
	}
}
