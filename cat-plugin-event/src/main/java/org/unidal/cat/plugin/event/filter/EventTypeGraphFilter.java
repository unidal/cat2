package org.unidal.cat.plugin.event.filter;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventTypeGraphFilter.ID)
public class EventTypeGraphFilter implements ReportFilter<EventReport> {
   public static final String ID = "type-graph";

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
   public EventReport screen(RemoteContext ctx, EventReport report) {
      String group = ctx.getProperty("group", null);
      String ip = ctx.getProperty("ip", null);
      String type = ctx.getProperty("type", null);
      TypeGraphScreener visitor = new TypeGraphScreener(report.getDomain(), group, ip, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventReport report) {
      TypeGraphTailor visitor = new TypeGraphTailor();

      report.accept(visitor);
   }

   private class TypeGraphScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private String m_type;

      private EventHolder m_holder = new EventHolder();

      private EventHolder m_all = new EventHolder();

      public TypeGraphScreener(String domain, String group, String ip, String type) {
         m_group = group;
         m_ip = ip;
         m_type = type;
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
            m_helper.mergeName(na, name);

            m_helper.mergeRanges(na.getRanges(), name.getRanges());
         } else {
            m_helper.mergeName(n, name);

            m_helper.mergeRanges(n.getRanges(), name.getRanges());
         }
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

         if (ta != null) {
            EventName na = ta.findOrCreateName(Constants.ALL);

            m_helper.mergeType(ta, type);
            m_all.setName(na);
         } else {
            EventName n = t.findOrCreateName(Constants.ALL);

            m_holder.setName(n);
         }

         m_helper.mergeType(t, type);

         for (EventName name : type.getNames().values()) {
            visitName(name);
         }
      }
   }

   private class TypeGraphTailor extends BaseVisitor {
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
