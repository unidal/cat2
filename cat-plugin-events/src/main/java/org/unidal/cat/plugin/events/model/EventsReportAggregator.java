package org.unidal.cat.plugin.events.model;

import java.util.Collection;

import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.filter.EventsHelper;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportAggregator.class, value = EventsConstants.NAME)
public class EventsReportAggregator extends ContainerHolder implements ReportAggregator<EventsReport> {
   @Inject
   private EventsHelper m_helper;

   @Override
   public EventsReport aggregate(ReportPeriod period, Collection<EventsReport> reports) {
      EventsReport aggregated = new EventsReport().setPeriod(period);

      if (reports.size() > 0) {
         EventsReportMerger merger = new EventsReportMerger(m_helper, aggregated);

         for (EventsReport report : reports) {
            report.accept(merger);
         }
      }

      return aggregated;
   }
}
