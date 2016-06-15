package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.Collection;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventTypeFilter.ID)
public class EventTypeFilter implements ReportFilter<EventReport> {
   public static final String ID = "type";

   @Inject
   private EventReportHelper m_helper;

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
      String ip = ctx.getProperty("ip", null);
      TypeScreener visitor = new TypeScreener(report.getDomain(), ip);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventReport report) {
      String ip = ctx.getProperty("ip", null);
      TypeTailor visitor = new TypeTailor(ip);

      report.accept(visitor);
   }

   private class TypeScreener extends BaseVisitor {
      private String m_ip;

      private EventHolder m_holder = new EventHolder();

      public TypeScreener(String domain, String ip) {
         m_ip = ip;
         m_holder.setReport(new EventReport(domain));
      }

      public EventReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitEventReport(EventReport report) {
         EventReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_ip == null || m_ip.equals(Constants.ALL)) {
            Machine m = r.findOrCreateMachine(Constants.ALL);
            Collection<Machine> machines = new ArrayList<Machine>(report.getMachines().values());

            m_holder.setMachine(m);

            for (Machine machine : machines) {
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
      public void visitType(EventType type) {
         EventType t = m_holder.getType();

         m_helper.mergeType(t, type);
      }
   }

   private class TypeTailor extends BaseVisitor {
      private String m_ip;

      private Machine m_machine;

      public TypeTailor(String ip) {
         m_ip = ip;
      }

      @Override
      public void visitEventReport(EventReport eventReport) {
         boolean all = m_ip == null || m_ip.equals(Constants.ALL);

         if (all) {
            m_machine = new Machine(Constants.ALL);
         } else {
            m_machine = new Machine(m_ip);

            Machine m = eventReport.findMachine(m_ip);
            eventReport.getMachines().clear();

            if (m != null) {
               eventReport.addMachine(m);
            }
         }

         super.visitEventReport(eventReport);

         eventReport.getMachines().clear();
         eventReport.addMachine(m_machine);
      }

      @Override
      public void visitType(EventType type) {
         type.getNames().clear();
         EventType t = m_machine.findOrCreateType(type.getId());
         m_helper.mergeType(t, type);
      }
   }
}
