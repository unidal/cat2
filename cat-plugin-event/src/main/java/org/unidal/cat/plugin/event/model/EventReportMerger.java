package org.unidal.cat.plugin.event.model;

import org.unidal.cat.plugin.event.filter.EventHelper;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.plugin.event.model.transform.DefaultMerger;

public class EventReportMerger extends DefaultMerger {
   private EventHelper m_helper;

   public EventReportMerger(EventHelper helper, EventReport eventReport) {
      super(eventReport);

      m_helper = helper;
   }

   @Override
   public void mergeMachine(Machine old, Machine other) {
      m_helper.mergeMachine(old, other);
   }

   @Override
   public void mergeName(EventName old, EventName other) {
      m_helper.mergeName(old, other);
   }

   @Override
   public void mergeRange(Range old, Range range) {
      old.setCount(old.getCount() + range.getCount());
      old.setFails(old.getFails() + range.getFails());
   }

   @Override
   public void mergeType(EventType old, EventType other) {
      m_helper.mergeType(old, other);
   }

   @Override
   public void visitEventReport(EventReport report) {
      super.visitEventReport(report);

      getEventReport().getDomainNames().addAll(report.getDomainNames());
      getEventReport().getIps().addAll(report.getIps());
   }
}
