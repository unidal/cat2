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

@Named(type = ReportFilter.class, value = EventsConstants.NAME + ":" + EventsNameGraphFilter.ID)
public class EventsNameGraphFilter implements ReportFilter<EventsReport> {
   public static final String ID = "name-graph";

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
      String name = ctx.getProperty("name", null);
      NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), group, bu, type, name);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventsReport report) {
      NameGraphTailor visitor = new NameGraphTailor();

      report.accept(visitor);
   }

   private class NameGraphScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private String m_type;

      private String m_name;

      private EventsHolder m_holder = new EventsHolder();

      private EventsHolder m_all = new EventsHolder();

      public NameGraphScreener(String domain, String group, String bu, String type, String name) {
         m_group = group;
         m_bu = bu;
         m_type = type;
         m_name = name;
         m_holder.setReport(new EventsReport());
      }

      public EventsReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitDepartment(EventsDepartment department) {
         EventsDepartment all = m_all.getDepartment();
         EventsDepartment m = m_holder.getDepartment();
         EventsType type = department.findType(m_type);

         if (all != null) {
            m_helper.mergeDepartment(all, department);
            m_helper.mergeDepartment(m, department);

            if (type != null) {
               EventsType ta = all.findOrCreateType(m_type);
               EventsType t = m.findOrCreateType(m_type);

               m_all.setType(ta);
               m_holder.setType(t);
               visitType(type);
            }
         } else {
            m_helper.mergeDepartment(m, department);

            if (type != null) {
               EventsType t = m.findOrCreateType(m_type);

               m_holder.setType(t);
               visitType(type);
            }
         }
      }

      @Override
      public void visitName(EventsName name) {
         EventsName na = m_all.getName();
         EventsName n = m_holder.getName();

         if (na != null) {
            m_helper.mergeName(n, name);

            n = na;
         }

         m_helper.mergeName(n, name);

         m_helper.mergeRanges(n.getRanges(), name.getRanges());
      }

      @Override
      public void visitEventsReport(EventsReport report) {
         EventsReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_group != null) {
            String domain = report.getDomain();

            for (EventsDepartment department : report.getDepartments().values()) {
               if (m_configService.isInGroup(domain, m_group, department.getId())) {
                  r.findOrCreateDepartment(department.getId());
               }
            }

            EventsDepartment m = r.findOrCreateDepartment(m_group);

            m_all.setDepartment(m);

            for (EventsDepartment department : report.getDepartments().values()) {
               if (m_configService.isInGroup(domain, m_group, department.getId())) {
                  m_holder.setDepartment(r.findDepartment(department.getId()));
                  visitDepartment(department);
               }
            }
         } else if (m_bu == null || m_bu.equals(Constants.ALL)) {
            for (EventsDepartment department : report.getDepartments().values()) {
               r.findOrCreateDepartment(department.getId());
            }

            EventsDepartment m = r.findOrCreateDepartment(Constants.ALL);

            m_all.setDepartment(m);

            for (EventsDepartment department : report.getDepartments().values()) {
               m_holder.setDepartment(r.findDepartment(department.getId()));
               visitDepartment(department);
            }
         } else {
            EventsDepartment department = report.findDepartment(m_bu);
            EventsDepartment m = r.findOrCreateDepartment(m_bu);

            if (department != null) {
               m_holder.setDepartment(m);
               visitDepartment(department);
            }
         }
      }

      @Override
      public void visitType(EventsType type) {
         EventsType ta = m_all.getType();
         EventsType t = m_holder.getType();
         EventsName name = type.findName(m_name);

         if (name != null) {
            if (ta != null) {
               EventsName na = ta.findOrCreateName(m_name);

               m_helper.mergeType(ta, type);
               m_all.setName(na);
            }

            EventsName n = t.findOrCreateName(m_name);

            m_helper.mergeType(t, type);
            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameGraphTailor extends BaseVisitor {
      @Override
      public void visitName(EventsName name) {
         name.setSuccessMessageUrl(null);
         name.setFailMessageUrl(null);

         super.visitName(name);
      }

      @Override
      public void visitType(EventsType type) {
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);

         super.visitType(type);
      }
   }
}
