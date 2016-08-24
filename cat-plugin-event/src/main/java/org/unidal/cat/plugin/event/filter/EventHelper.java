package org.unidal.cat.plugin.event.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.lookup.annotation.Named;

@Named(type = EventHelper.class)
public class EventHelper {
   public void mergeMachine(Machine old, Machine other) {
      // nothing to do
   }

   public void mergeName(EventName dst, EventName src) {
      long totalCount = dst.getTotalCount() + src.getTotalCount();

      dst.setTotalCount(totalCount);
      dst.setFailCount(dst.getFailCount() + src.getFailCount());
      dst.setTps(dst.getTps() + src.getTps());

      if (dst.getTotalCount() > 0) {
         dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
      }

      if (dst.getSuccessMessageUrl() == null) {
         dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
      }

      if (dst.getFailMessageUrl() == null) {
         dst.setFailMessageUrl(src.getFailMessageUrl());
      }
   }

   public void mergeRanges(List<Range> dst, List<Range> src) {
      Map<Integer, Integer> map = new HashMap<Integer, Integer>();

      for (int i = dst.size() - 1; i >= 0; i--) {
         Range duration = dst.get(i);

         map.put(duration.getValue(), i);
      }

      for (int i = 0; i < src.size(); i++) {
         Range duration = src.get(i);
         Integer index = map.get(duration.getValue());
         Range oldRange;

         if (index == null) {
            oldRange = new Range(duration.getValue());
            dst.add(oldRange);
         } else {
            oldRange = dst.get(index);
         }

         oldRange.setCount(oldRange.getCount() + duration.getCount());
         oldRange.setFails(oldRange.getFails() + duration.getFails());
      }
   }

   public void mergeReport(EventReport dst, EventReport src) {
      dst.mergeAttributes(src);
      dst.getDomainNames().addAll(src.getDomainNames());
      dst.getIps().addAll(src.getIps());
   }

   public void mergeType(EventType dst, EventType src) {
      long totalCountSum = dst.getTotalCount() + src.getTotalCount();

      dst.setTotalCount(totalCountSum);
      dst.setFailCount(dst.getFailCount() + src.getFailCount());
      dst.setTps(dst.getTps() + src.getTps());

      if (dst.getTotalCount() > 0) {
         dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
      }

      if (dst.getSuccessMessageUrl() == null) {
         dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
      }

      if (dst.getFailMessageUrl() == null) {
         dst.setFailMessageUrl(src.getFailMessageUrl());
      }
   }
}
