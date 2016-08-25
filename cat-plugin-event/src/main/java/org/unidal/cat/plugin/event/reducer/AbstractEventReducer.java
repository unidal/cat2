package org.unidal.cat.plugin.event.reducer;

import java.util.List;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.filter.EventHelper;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.plugin.event.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.lookup.annotation.Inject;

public abstract class AbstractEventReducer implements ReportReducer<EventReport> {
   @Inject
   private EventHelper m_helper;

   protected abstract int getRangeValue(EventReport report, Range range);

   @Override
   public String getReportName() {
      return EventConstants.NAME;
   }

   @Override
   public EventReport reduce(List<EventReport> reports) {
      EventReport r = new EventReport();

      if (!reports.isEmpty()) {
         EventReport first = reports.get(0);
         Merger merger = new Merger(r);

         r.setDomain(first.getDomain());

         for (EventReport report : reports) {
            report.accept(merger.setMapping(new ValueMapping(report)));
         }

         ReportPeriod period = getPeriod();

         r.setPeriod(period);
         r.setStartTime(period.getStartTime(first.getStartTime()));
      }

      return r;
   }

   protected class Merger extends DefaultMerger {
      private RangeMapping m_mapping;

      public Merger(EventReport report) {
         super(report);
      }

      @Override
      protected void mergeMachine(Machine old, Machine other) {
         m_helper.mergeMachine(old, other);
      }

      @Override
      protected void mergeName(EventName old, EventName other) {
         m_helper.mergeName(old, other);
      }

      @Override
      protected void mergeRange(Range old, Range range) {
         old.setCount(old.getCount() + range.getCount());
         old.setFails(old.getFails() + range.getFails());
      }

      @Override
      protected void mergeType(EventType old, EventType other) {
         m_helper.mergeType(old, other);
      }

      public Merger setMapping(RangeMapping mapping) {
         m_mapping = mapping;
         return this;
      }

      protected void visitNameChildren(EventName to, EventName from) {
         for (Range source : from.getRanges()) {
            int value = m_mapping.getValue(source);
            Range r = to.findOrCreateRange(value);

            getObjects().push(r);
            source.accept(this);
            getObjects().pop();
         }
      }
   }

   protected static interface RangeMapping {
      public int getValue(Range range);
   }

   class ValueMapping implements RangeMapping {
      private EventReport m_report;

      public ValueMapping(EventReport report) {
         m_report = report;
      }

      @Override
      public int getValue(Range range) {
         return getRangeValue(m_report, range);
      }
   }
}
