package org.unidal.cat.plugin.event;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = EventConstants.NAME)
public class EventReportManager extends AbstractReportManager<EventReport> {
   @Override
   public int getThreadsCount() {
      return 2;
   }
}
