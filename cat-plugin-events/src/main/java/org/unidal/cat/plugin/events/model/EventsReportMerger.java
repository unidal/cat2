package org.unidal.cat.plugin.events.model;

import org.unidal.cat.plugin.events.filter.EventsHelper;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.plugin.events.model.transform.DefaultMerger;

public class EventsReportMerger extends DefaultMerger {
   private EventsHelper m_helper;

   public EventsReportMerger(EventsHelper helper, EventsReport eventsReport) {
      super(eventsReport);

      m_helper = helper;
   }

   @Override
   public void mergeName(EventsName old, EventsName other) {
      m_helper.mergeName(old, other);
   }

   @Override
   public void mergeRange(EventsRange old, EventsRange other) {
      m_helper.mergeRange(old, other);
   }

   @Override
   public void mergeType(EventsType old, EventsType other) {
      m_helper.mergeType(old, other);
   }

   @Override
   public void visitEventsReport(EventsReport report) {
      super.visitEventsReport(report);

      getEventsReport().getBus().addAll(report.getBus());
   }
}
