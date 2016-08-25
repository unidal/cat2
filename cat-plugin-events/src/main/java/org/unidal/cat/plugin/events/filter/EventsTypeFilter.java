package org.unidal.cat.plugin.events.filter;

import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.EventsHolder;
import org.unidal.cat.plugin.events.model.entity.EventsDepartment;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.plugin.events.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventsConstants.NAME + ":" + EventsTypeFilter.ID)
public class EventsTypeFilter implements ReportFilter<EventsReport> {
   public static final String ID = "type";

   @Inject
   private EventsHelper m_helper;

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
      TypeScreener visitor = new TypeScreener(report.getDomain(), group, bu);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventsReport report) {
   }

   private class TypeScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private EventsHolder m_holder = new EventsHolder();

      public TypeScreener(String domain, String group, String bu) {
         m_group = group;
         m_bu = bu;
         m_holder.setReport(new EventsReport());
      }

      public EventsReport getReport() {
         return m_holder.getReport();
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
      public void visitDepartment(EventsDepartment department) {
         EventsDepartment d = m_holder.getDepartment();

         m_helper.mergeDepartment(d, department);

         for (EventsType type : department.getTypes().values()) {
            EventsType t = d.findOrCreateType(type.getId());

            m_holder.setType(t);
            visitType(type);
         }
      }

      @Override
      public void visitType(EventsType type) {
         m_helper.mergeType(m_holder.getType(), type);
      }
   }
}
