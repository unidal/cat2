package org.unidal.cat.plugin.events.reducer;

import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.helper.Dates;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportReducer.class, value = EventsConstants.NAME + ":" + EventsWeeklyReducer.ID)
public class EventsWeeklyReducer extends AbstractEventsReducer implements ReportReducer<EventsReport> {
	public static final String ID = WEEKLY;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ReportPeriod getPeriod() {
		return ReportPeriod.WEEK;
	}

	@Override
	protected int getRangeValue(EventsReport report, EventsRange range) {
		int day = Dates.from(report.getStartTime()).dayOfWeek();

		return day;
	}
}
