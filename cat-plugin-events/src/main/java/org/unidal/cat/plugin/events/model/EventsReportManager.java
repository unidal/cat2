package org.unidal.cat.plugin.events.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.config.DomainOrgConfigService;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.filter.EventHolder;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.plugin.event.model.transform.BaseVisitor;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.config.EventsConfigService;
import org.unidal.cat.plugin.events.model.entity.EventsDomain;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.ReportManagerManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = EventsConstants.NAME)
public class EventsReportManager extends AbstractReportManager<EventsReport> {
   @Inject
   private ReportManagerManager m_rmm;

   @Inject
   private DomainOrgConfigService m_orgConfigService;

   @Inject
   private EventsConfigService m_configService;

   private EventsReport buildReport(int hour, String bu) {
      ReportManager<EventReport> rm = m_rmm.getReportManager(EventConstants.NAME);
      List<Map<String, EventReport>> list = rm.getLocalReports(hour);
      EventsReportMaker maker = new EventsReportMaker();

      for (Map<String, EventReport> map : list) {
         for (Map.Entry<String, EventReport> e : map.entrySet()) {
            if (bu == null || m_orgConfigService.isIn(bu, e.getKey())) {
               e.getValue().accept(maker);
            }
         }
      }

      m_configService.reset();
      EventsReport report = maker.getReport();
      return report;
   }

   /**
    * prepares events report for persistance.
    */
   @Override
   @SuppressWarnings("unchecked")
   public List<Map<String, EventsReport>> getLocalReports(int hour) {
      EventsReport report = buildReport(hour, null);
      Map<String, EventsReport> map = new HashMap<String, EventsReport>();

      map.put(com.dianping.cat.Constants.ALL, report);
      return Arrays.asList(map);
   }

   /**
    * builds events report from event reports dynamically
    */
   @Override
   public List<EventsReport> getReports(ReportPeriod period, Date startTime, String domain,
         Map<String, String> properties) throws IOException {
      String bu = properties == null ? null : properties.get("bu");
      int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime.getTime());
      EventsReport report = buildReport(hour, bu);

      return Arrays.asList(report);
   }

   @Override
   public int getThreadsCount() {
      return 1;
   }

   class EventsReportMaker extends BaseVisitor {
      private EventHolder m_t;

      private EventsHolder m_ts;

      public EventsReportMaker() {
         m_t = new EventHolder();
         m_ts = new EventsHolder();
         m_ts.setReport(new EventsReport());
      }

      public EventsReport getReport() {
         return m_ts.getReport();
      }

      private void mergeDomain(EventsDomain dst, EventName src) {
         long totalCountSum = dst.getTotalCount() + src.getTotalCount();

         dst.setTotalCount(totalCountSum);
         dst.setFailCount(dst.getFailCount() + src.getFailCount());
         dst.setTps(dst.getTps() + src.getTps());

         if (dst.getTotalCount() > 0) {
            dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
         }
      }

      private void mergeName(EventsName dst, EventName src) {
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

      private void mergeRanges(List<EventsRange> dst, List<Range> src) {
         Map<Integer, Integer> map = new HashMap<Integer, Integer>();

         for (int i = dst.size() - 1; i >= 0; i--) {
            EventsRange duration = dst.get(i);

            map.put(duration.getValue(), i);
         }

         for (int i = 0; i < src.size(); i++) {
            Range duration = src.get(i);
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

      private void mergeType(EventsType dst, EventType src) {
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

      @Override
      public void visitName(EventName name) {
         if (m_configService.isEligible(m_ts.getType().getId(), name.getId())) {
            EventsName n = m_ts.getType().findOrCreateName(name.getId());
            EventsDomain d = n.findOrCreateDomain(m_t.getReport().getDomain());

            m_ts.setName(n);
            mergeName(n, name);

            m_ts.setDomain(d);
            mergeDomain(d, name);

            mergeRanges(n.getRanges(), name.getRanges());

            super.visitName(name);
         }
      }

      @Override
      public void visitEventReport(EventReport report) {
         EventsReport r = m_ts.getReport();
         String d = m_orgConfigService.findDepartment(report.getDomain());

         r.setPeriod(report.getPeriod()).addBu(d);
         r.setStartTime(report.getStartTime()).setEndTime(report.getEndTime());
         m_t.setReport(report);
         m_ts.setDepartment(r.findOrCreateDepartment(d));

         super.visitEventReport(report);
      }

      @Override
      public void visitType(EventType type) {
         if (m_configService.isEligible(type.getId())) {
            EventsType t = m_ts.getDepartment().findOrCreateType(type.getId());

            m_ts.setType(t);
            mergeType(t, type);

            super.visitType(type);
         }
      }
   }
}
