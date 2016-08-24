package org.unidal.cat.plugin.event.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventTypeFilter.ID)
public class EventTypeFilter implements ReportFilter<EventReport> {
   public static final String ID = "type";

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
      TypeScreener visitor = new TypeScreener(report.getDomain(), group, ip);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventReport report) {
      TypeTailor visitor = new TypeTailor();

      report.accept(visitor);
   }

   private class TypeScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private EventHolder m_holder = new EventHolder();

      public TypeScreener(String domain, String group, String ip) {
         m_group = group;
         m_ip = ip;
         m_holder.setReport(new EventReport(domain));
      }

      public EventReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine m = m_holder.getMachine();

         m_helper.mergeMachine(m, machine);

         Collection<EventType> types = new ArrayList<EventType>(machine.getTypes().values());

         for (EventType type : types) {
            EventType t = m.findOrCreateType(type.getId());

            m_holder.setType(t);
            visitType(type);
         }
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
      }
   }

   private class TypeTailor extends BaseVisitor {
      @Override
      public void visitType(EventType type) {
         type.getNames().clear();
      }
   }
}
