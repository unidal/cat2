package org.unidal.cat.plugin.event.filter;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.transform.BaseVisitor;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventNameFilter.ID)
public class EventNameFilter implements ReportFilter<EventReport> {
   public static final String ID = "name";

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
      NameScreener visitor = new NameScreener(report.getDomain(), group, ip, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteReportContext ctx, EventReport report) {
      NameTailor visitor = new NameTailor();

      report.accept(visitor);
   }

   private class NameScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private String m_type;

      private EventHolder m_holder = new EventHolder();

      public NameScreener(String domain, String group, String ip, String type) {
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
         Machine m = m_holder.getMachine();
         EventType t = m.findOrCreateType(m_type);
         EventType type = machine.findType(m_type);

         m_helper.mergeMachine(m, machine);
         m_holder.setType(t);

         if (type != null) {
            visitType(type);
         }
      }

      @Override
      public void visitName(EventName name) {
         EventName n = m_holder.getName();

         m_helper.mergeName(n, name);
      }

      @Override
      public void visitEventReport(EventReport report) {
         EventReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_group != null) {
            Machine m = r.findOrCreateMachine(m_group);
            String domain = report.getDomain();

            m_holder.setMachine(m);

            for (Machine machine : report.getMachines().values()) {
               if (m_configService.isInGroup(domain, m_group, machine.getIp())) {
                  visitMachine(machine);
               }
            }
         } else if (m_ip == null || m_ip.equals(Constants.ALL)) {
            Machine m = r.findOrCreateMachine(Constants.ALL);

            m_holder.setMachine(m);

            for (Machine machine : report.getMachines().values()) {
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
         EventType t = m_holder.getType();

         m_helper.mergeType(t, type);

         for (EventName name : type.getNames().values()) {
            EventName n = t.findOrCreateName(name.getId());

            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameTailor extends BaseVisitor {
      @Override
      public void visitName(EventName name) {
         name.getRanges().clear();
      }
   }
}
