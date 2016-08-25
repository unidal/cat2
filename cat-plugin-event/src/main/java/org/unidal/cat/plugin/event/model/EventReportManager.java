package org.unidal.cat.plugin.event.model;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = EventConstants.NAME)
public class EventReportManager extends AbstractReportManager<EventReport> {
   @Override
   public int getThreadsCount() {
      return 2;
   }
}
