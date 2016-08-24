package org.unidal.cat.plugin.events.reducer;

import java.util.List;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.events.filter.EventsHelper;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.plugin.events.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.lookup.annotation.Inject;

public abstract class AbstractEventsReducer implements ReportReducer<EventsReport> {
   @Inject
   private EventsHelper m_helper;

   protected abstract int getRangeValue(EventsReport report, EventsRange range);

   @Override
   public String getReportName() {
      return EventConstants.NAME;
   }

   @Override
   public EventsReport reduce(List<EventsReport> reports) {
      EventsReport r = new EventsReport();

      if (!reports.isEmpty()) {
         EventsReport first = reports.get(0);
         Merger merger = new Merger(r);

         for (EventsReport report : reports) {
            report.accept(merger.setMapping(new ValueMapping(report)));
         }

         ReportPeriod period = getPeriod();

         r.setPeriod(period);
         r.setStartTime(period.getStartTime(first.getStartTime()));
      }

      return r;
   }

   protected class Merger extends DefaultMerger {
      private EventsRangeMapping m_mapping;

      public Merger(EventsReport report) {
         super(report);
      }

      @Override
      protected void mergeName(EventsName old, EventsName other) {
         m_helper.mergeName(old, other);
      }

      @Override
      protected void mergeRange(EventsRange old, EventsRange other) {
         m_helper.mergeRange(old, other);
      }

      @Override
      protected void mergeType(EventsType old, EventsType other) {
         m_helper.mergeType(old, other);
      }

      public Merger setMapping(EventsRangeMapping mapping) {
         m_mapping = mapping;
         return this;
      }

      protected void visitNameChildren(EventsName to, EventsName from) {
         for (EventsRange source : from.getRanges()) {
            int value = m_mapping.getValue(source);
            EventsRange r = to.findOrCreateRange(value);

            getObjects().push(r);
            source.accept(this);
            getObjects().pop();
         }
      }
   }

   protected static interface EventsRangeMapping {
      public int getValue(EventsRange range);
   }

   class ValueMapping implements EventsRangeMapping {
      private EventsReport m_report;

      public ValueMapping(EventsReport report) {
         m_report = report;
      }

      @Override
      public int getValue(EventsRange range) {
         return getRangeValue(m_report, range);
      }
   }
}
