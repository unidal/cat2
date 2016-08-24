package org.unidal.cat.plugin.event.model;

import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.plugin.event.model.transform.DefaultMerger;

public class EventReportMerger extends DefaultMerger {
   public EventReportMerger(EventReport eventReport) {
      super(eventReport);
   }

   @Override
   public void mergeMachine(Machine old, Machine machine) {
   }

   @Override
   public void mergeName(EventName old, EventName other) {
      long totalCountSum = old.getTotalCount() + other.getTotalCount();

      old.setTotalCount(totalCountSum);
      old.setFailCount(old.getFailCount() + other.getFailCount());
      old.setTps(old.getTps() + other.getTps());

      if (old.getTotalCount() > 0) {
         old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
      }

      if (old.getSuccessMessageUrl() == null) {
         old.setSuccessMessageUrl(other.getSuccessMessageUrl());
      }

      if (old.getFailMessageUrl() == null) {
         old.setFailMessageUrl(other.getFailMessageUrl());
      }
   }

   @Override
   public void mergeRange(Range old, Range range) {
      old.setCount(old.getCount() + range.getCount());
      old.setFails(old.getFails() + range.getFails());
   }

   @Override
   public void mergeType(EventType old, EventType other) {
      long totalCountSum = old.getTotalCount() + other.getTotalCount();

      old.setTotalCount(totalCountSum);
      old.setFailCount(old.getFailCount() + other.getFailCount());
      old.setTps(old.getTps() + other.getTps());

      if (old.getTotalCount() > 0) {
         old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
      }

      if (old.getSuccessMessageUrl() == null) {
         old.setSuccessMessageUrl(other.getSuccessMessageUrl());
      }

      if (old.getFailMessageUrl() == null) {
         old.setFailMessageUrl(other.getFailMessageUrl());
      }
   }

   @Override
   public void visitEventReport(EventReport eventReport) {
      super.visitEventReport(eventReport);
      getEventReport().getDomainNames().addAll(eventReport.getDomainNames());
      getEventReport().getIps().addAll(eventReport.getIps());
   }
}
