package org.unidal.cat.plugin.events.filter;

import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.EventsHolder;
import org.unidal.cat.plugin.events.model.entity.EventsDepartment;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.plugin.events.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventsConstants.NAME + ":" + EventsNameFilter.ID)
public class EventsNameFilter implements ReportFilter<EventsReport> {
   public static final String ID = "name";

   @Inject
   private EventsHelper m_helper;

   @Inject
   private DomainGroupConfigService m_configService;

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getReportName() {
      return EventsConstants.NAME;
   }

   @Override
   public EventsReport screen(RemoteContext ctx, EventsReport report) {
      String group = ctx.getProperty("group", null);
      String bu = ctx.getProperty("bu", null);
      String type = ctx.getProperty("type", null);
      NameScreener visitor = new NameScreener(report.getDomain(), group, bu, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventsReport report) {
   }

   private class NameScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private String m_type;

      private EventsHolder m_holder = new EventsHolder();

      public NameScreener(String domain, String group, String bu, String type) {
         m_group = group;
         m_bu = bu;
         m_type = type;
         m_holder.setReport(new EventsReport());
      }

      public EventsReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitDepartment(EventsDepartment department) {
         EventsDepartment d = m_holder.getDepartment();
         EventsType t = d.findOrCreateType(m_type);
         EventsType type = department.findType(m_type);

         m_helper.mergeDepartment(d, department);
         m_holder.setType(t);

         if (type != null) {
            visitType(type);
         }
      }

      @Override
      public void visitName(EventsName name) {
         EventsName n = m_holder.getName();

         m_helper.mergeName(n, name);
      }

      @Override
      public void visitEventsReport(EventsReport report) {
         EventsReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_group != null) {
            // TODO department grouping?
         } else if (m_bu == null) {
            EventsDepartment d = r.findOrCreateDepartment(Constants.ALL);

            m_holder.setDepartment(d);

            for (EventsDepartment department : report.getDepartments().values()) {
               visitDepartment(department);
            }
         } else {
            EventsDepartment department = report.findOrCreateDepartment(m_bu);
            EventsDepartment d = r.findOrCreateDepartment(m_bu);

            if (department != null) {
               m_holder.setDepartment(d);
               visitDepartment(department);
            }
         }
      }

      @Override
      public void visitType(EventsType type) {
         EventsType t = m_holder.getType();

         m_helper.mergeType(t, type);

         for (EventsName name : type.getNames().values()) {
            EventsName n = t.findOrCreateName(name.getId());

            m_holder.setName(n);
            visitName(name);
         }
      }
   }
}
