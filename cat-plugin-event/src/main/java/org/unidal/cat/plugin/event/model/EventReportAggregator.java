package org.unidal.cat.plugin.event.model;

import java.util.Collection;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.filter.EventHelper;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportAggregator.class, value = EventConstants.NAME)
public class EventReportAggregator extends ContainerHolder implements ReportAggregator<EventReport> {
   @Inject
   private EventHelper m_helper;

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
