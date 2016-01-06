package org.unidal.cat.plugin.event;

import java.util.Collection;

import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportAggregator;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;

@Named(type = ReportAggregator.class, value = EventConstants.ID)
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
}
