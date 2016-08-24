package org.unidal.cat.plugin.events.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.plugin.events.model.entity.EventsDepartment;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.lookup.annotation.Named;

@Named(type = EventsHelper.class)
public class EventsHelper {
   public void mergeDepartment(EventsDepartment dst, EventsDepartment src) {
      // Do nothing
   }

   public void mergeName(EventsName dst, EventsName src) {
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

   public void mergeRanges(List<EventsRange> dst, List<EventsRange> src) {
      Map<Integer, Integer> map = new HashMap<Integer, Integer>();

      for (int i = dst.size() - 1; i >= 0; i--) {
         EventsRange duration = dst.get(i);

         map.put(duration.getValue(), i);
      }

      for (int i = 0; i < src.size(); i++) {
         EventsRange duration = src.get(i);
         Integer index = map.get(duration.getValue());
         EventsRange oldRange;

         if (index == null) {
            oldRange = new EventsRange(duration.getValue());
            dst.add(oldRange);
         } else {
            oldRange = dst.get(index);
         }

         oldRange.setCount(oldRange.getCount() + duration.getCount());
         oldRange.setFails(oldRange.getFails() + duration.getFails());
      }
   }

   public void mergeReport(EventsReport dst, EventsReport src) {
      dst.mergeAttributes(src);
      dst.getBus().addAll(src.getBus());
   }

   public void mergeType(EventsType dst, EventsType src) {
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

   public void mergeRange(EventsRange dst, EventsRange src) {
      dst.setCount(dst.getCount() + src.getCount());
      dst.setFails(dst.getFails() + src.getFails());
   }
}
