package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventName;
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

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventNameGraphFilter.ID)
public class EventNameGraphFilter implements ReportFilter<EventReport> {
   public static final String ID = "name-graph";

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
      String type = ctx.getProperty("type", null);
      String name = ctx.getProperty("name", null);
      String ip = ctx.getProperty("ip", null);
      NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), ip, type, name);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventReport report) {
      String type = ctx.getProperty("type", null);
      String name = ctx.getProperty("name", null);
      String ip = ctx.getProperty("ip", null);
      NameGraphTailor visitor = new NameGraphTailor(ip, type, name);

      report.accept(visitor);
   }

   private class NameGraphScreener extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private String m_name;

      private EventHolder m_holder = new EventHolder();

      private EventHolder m_all = new EventHolder();

      public NameGraphScreener(String domain, String ip, String type, String name) {
         m_type = type;
         m_name = name;
         m_ip = ip;
         m_holder.setReport(new EventReport(domain));
      }

      public EventReport getReport() {
         return m_holder.getReport();
      }

      private void mergeName(EventName n, EventName name) {
         m_helper.mergeName(n, name);
         n.setSuccessMessageUrl(null);
         n.setFailMessageUrl(null);
      }

      private void mergeType(EventType t, EventType type) {
         m_helper.mergeType(t, type);
         t.setSuccessMessageUrl(null);
         t.setFailMessageUrl(null);
      }

      @Override
      public void visitEventReport(EventReport report) {
         EventReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_ip == null || m_ip.equals(Constants.ALL)) {
            Collection<Machine> machines = new ArrayList<Machine>(report.getMachines().values());

            for (Machine machine : machines) {
               r.findOrCreateMachine(machine.getIp());
            }

            Machine machineAll = r.findOrCreateMachine(Constants.ALL);

            m_all.setMachine(machineAll);

            for (Machine machine : machines) {
               Machine m = r.findMachine(machine.getIp());

               m_holder.setMachine(m);
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
         Machine machineAll = m_all.getMachine();
         Machine m = m_holder.getMachine();
         EventType type = machine.findType(m_type);

         if (machineAll != null) {
            m_helper.mergeMachine(machineAll, machine);
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               EventType ta = machineAll.findOrCreateType(m_type);
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
            mergeName(n, name);

            n = na;
         }

         mergeName(n, name);

         m_helper.mergeRanges(n.getRanges(), name.getRanges());
      }

      @Override
      public void visitType(EventType type) {
         EventType ta = m_all.getType();
         EventType t = m_holder.getType();
         EventName name = type.findName(m_name);

         if (name != null) {
            if (ta != null) {
               EventName na = ta.findOrCreateName(m_name);

               mergeType(ta, type);
               m_all.setName(na);
            }

            EventName n = t.findOrCreateName(m_name);

            mergeType(t, type);
            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameGraphTailor extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private String m_name;

      private EventHolder m_holder = new EventHolder();

      public NameGraphTailor(String ip, String type, String name) {
         m_ip = ip;
         m_type = type;
         m_name = name;
      }

      @Override
      public void visitEventReport(EventReport eventReport) {
         boolean all = m_ip == null || m_ip.equals(Constants.ALL);

         if (all) {
            Machine machine = new Machine(Constants.ALL);

            m_holder.setMachine(machine);
         } else {
            Machine machine = new Machine(m_ip);
            Machine m = eventReport.findMachine(m_ip);

            eventReport.getMachines().clear();
            if (null != m) {
               eventReport.addMachine(m);
            }
            m_holder.setMachine(machine);
         }

         super.visitEventReport(eventReport);

         eventReport.addMachine(m_holder.getMachine());
      }

      @Override
      public void visitMachine(Machine machine) {
         EventType type = machine.findType(m_type);

         machine.getTypes().clear();

         if (type != null) {
            EventType t = m_holder.getMachine().findOrCreateType(type.getId());

            machine.addType(type);
            m_helper.mergeType(t, type);
            m_holder.setType(t);
         }

         super.visitMachine(machine);
      }

      @Override
      public void visitType(EventType type) {
         EventType t = m_holder.getType();
         EventName n = t.findOrCreateName(m_name);
         EventName name = type.findName(m_name);

         t.setSuccessMessageUrl(null);
         t.setFailMessageUrl(null);
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);
         type.getNames().clear();

         if (name != null) {
            m_helper.mergeName(n, name);
            n.setSuccessMessageUrl(null);
            n.setFailMessageUrl(null);
            m_helper.mergeRanges(n.getRanges(), name.getRanges());

            type.addName(name);

            name.getRanges().clear();
            name.setSuccessMessageUrl(null);
            name.setFailMessageUrl(null);
         }
      }
   }
}
