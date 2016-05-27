package org.unidal.cat.plugin.event;

import java.util.Collection;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;

@Named(type = ReportAggregator.class, value = EventConstants.NAME)
public class EventReportAggregator implements ReportAggregator<EventReport> {
	@Override
	public EventReport aggregate(ReportPeriod period, Collection<EventReport> reports) {
		EventReport aggregated = new EventReport();

		if (reports.size() > 0) {
			EventReportMerger merger = new EventReportMerger(aggregated);

			// must be same domain
			aggregated.setDomain(reports.iterator().next().getDomain());

			for (EventReport report : reports) {
				report.accept(merger);
			}
		}

		return aggregated;
	}

	@Override
	public EventReport makeAll(ReportPeriod period, Collection<EventReport> reports) {
		return null;
	}
}
