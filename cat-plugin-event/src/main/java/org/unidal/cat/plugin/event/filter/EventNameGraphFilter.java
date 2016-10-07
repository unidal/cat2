package org.unidal.cat.plugin.event.filter;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.transform.BaseVisitor;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventNameGraphFilter.ID)
public class EventNameGraphFilter implements ReportFilter<EventReport> {
   public static final String ID = "name-graph";

   @Inject
   private EventHelper m_helper;

   @Inject
   private DomainGroupConfigService m_configService;

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getReportName() {
      return EventConstants.NAME;
   }

   @Override
   public EventReport screen(RemoteReportContext ctx, EventReport report) {
      String group = ctx.getProperty("group", null);
      String ip = ctx.getProperty("ip", null);
      String type = ctx.getProperty("type", null);
      String name = ctx.getProperty("name", null);
      NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), group, ip, type, name);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteReportContext ctx, EventReport report) {
      NameGraphTailor visitor = new NameGraphTailor();

      report.accept(visitor);
   }

   private class NameGraphScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private String m_type;

      private String m_name;

      private EventHolder m_holder = new EventHolder();

      private EventHolder m_all = new EventHolder();

      public NameGraphScreener(String domain, String group, String ip, String type, String name) {
         m_group = group;
         m_ip = ip;
         m_type = type;
         m_name = name;
         m_holder.setReport(new EventReport(domain));
      }

      public EventReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine all = m_all.getMachine();
         Machine m = m_holder.getMachine();
         EventType type = machine.findType(m_type);

         if (all != null) {
            m_helper.mergeMachine(all, machine);
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               EventType ta = all.findOrCreateType(m_type);
               EventType t = m.findOrCreateType(m_type);

               m_all.setType(ta);
               m_holder.setType(t);
               visitType(type);
            }
         } else {
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               EventType t = m.findOrCreateType(m_type);

               m_holder.setType(t);
               visitType(type);
            }
         }
      }

      @Override
      public void visitName(EventName name) {
         EventName na = m_all.getName();
         EventName n = m_holder.getName();

         if (na != null) {
            m_helper.mergeName(n, name);

            n = na;
         }

         m_helper.mergeName(n, name);

         m_helper.mergeRanges(n.getRanges(), name.getRanges());
      }

      @Override
      public void visitEventReport(EventReport report) {
         EventReport r = m_holder.getReport();
         boolean isAll = (m_ip == null ? true : m_ip.equals(Constants.ALL));

         m_helper.mergeReport(r, report);

         if (m_group != null && isAll) {
            String domain = report.getDomain();

            for (Machine machine : report.getMachines().values()) {
               if (m_configService.isInGroup(domain, m_group, machine.getIp())) {
                  r.findOrCreateMachine(machine.getIp());
               }
            }

            Machine m = r.findOrCreateMachine(Constants.ALL);

            m_all.setMachine(m);

            for (Machine machine : report.getMachines().values()) {
               if (m_configService.isInGroup(domain, m_group, machine.getIp())) {
                  m_holder.setMachine(r.findMachine(machine.getIp()));
                  visitMachine(machine);
               }
            }
         } else if (isAll) {
            for (Machine machine : report.getMachines().values()) {
               r.findOrCreateMachine(machine.getIp());
            }

            Machine m = r.findOrCreateMachine(Constants.ALL);

            m_all.setMachine(m);

            for (Machine machine : report.getMachines().values()) {
               m_holder.setMachine(r.findMachine(machine.getIp()));
               visitMachine(machine);
            }
         } else {
            Machine machine = report.findMachine(m_ip);
            Machine m = r.findOrCreateMachine(m_ip);

            if (machine != null) {
               m_holder.setMachine(m);
               visitMachine(machine);
            }
         }
      }

      @Override
      public void visitType(EventType type) {
         EventType ta = m_all.getType();
         EventType t = m_holder.getType();
         EventName name = type.findName(m_name);

         if (name != null) {
            if (ta != null) {
               EventName na = ta.findOrCreateName(m_name);

               m_helper.mergeType(ta, type);
               m_all.setName(na);
            }

            EventName n = t.findOrCreateName(m_name);

            m_helper.mergeType(t, type);
            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameGraphTailor extends BaseVisitor {
      @Override
      public void visitName(EventName name) {
         name.setSuccessMessageUrl(null);
         name.setFailMessageUrl(null);

         super.visitName(name);
      }

      @Override
      public void visitType(EventType type) {
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);

         super.visitType(type);
      }
   }
}
