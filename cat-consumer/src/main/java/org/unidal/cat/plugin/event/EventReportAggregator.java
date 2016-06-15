package org.unidal.cat.plugin.event;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.event.filter.EventReportHelper;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Collection;

@Named(type = ReportAggregator.class, value = EventConstants.NAME)
public class EventReportAggregator implements ReportAggregator<EventReport> {
   @Inject
   private ProjectService m_projectService;

   @Inject
   private EventReportHelper m_helper;

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
      EventReport all = new EventReport();

      if (reports.size() > 0) {
         all.setDomain(Constants.ALL);
         all.setStartTime(reports.iterator().next().getStartTime());
         all.setEndTime(reports.iterator().next().getEndTime());
         all.setPeriod(reports.iterator().next().getPeriod());

         EventAllReportMaker maker = new EventAllReportMaker(all, m_projectService, m_helper);

         for (EventReport report : reports) {
            report.accept(maker);
         }
      }

      return all;
   }
}
