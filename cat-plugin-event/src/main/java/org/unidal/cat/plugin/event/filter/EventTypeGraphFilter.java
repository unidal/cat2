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

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventTypeGraphFilter.ID)
public class EventTypeGraphFilter implements ReportFilter<EventReport> {
   public static final String ID = "type-graph";

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
      String ip = ctx.getProperty("ip", null);
      TypeGraphScreener visitor = new TypeGraphScreener(report.getDomain(), ip, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, EventReport report) {
      String type = ctx.getProperty("type", null);
      String ip = ctx.getProperty("ip", null);
      TypeGraphTailor visitor = new TypeGraphTailor(ip, type);

      report.accept(visitor);
   }

   private class TypeGraphScreener extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private EventHolder m_holder = new EventHolder();

      private EventHolder m_all = new EventHolder();

      public TypeGraphScreener(String domain, String ip, String type) {
         m_type = type;
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
            mergeName(na, name);

            m_helper.mergeRanges(na.getRanges(), name.getRanges());
         } else {
            mergeName(n, name);

            m_helper.mergeRanges(n.getRanges(), name.getRanges());
         }
      }

      @Override
      public void visitType(EventType type) {
         EventType ta = m_all.getType();
         EventType t = m_holder.getType();

         if (ta != null) {
            EventName na = ta.findOrCreateName(Constants.ALL);

            mergeType(ta, type);
            m_all.setName(na);
         } else {
            EventName n = t.findOrCreateName(Constants.ALL);

            m_holder.setName(n);
         }

         mergeType(t, type);

         Collection<EventName> names = new ArrayList<EventName>(type.getNames().values());

         for (EventName name : names) {
            visitName(name);
         }
      }
   }

   private class TypeGraphTailor extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private EventHolder m_holder = new EventHolder();

      public TypeGraphTailor(String ip, String type) {
         m_ip = ip;
         m_type = type;
      }

      @Override
      public void visitEventReport(EventReport report) {
         if (m_ip == null || m_ip.equals(Constants.ALL)) {
            Machine m = new Machine(Constants.ALL);

            m_holder.setMachine(m);
         } else {
            Machine machine = report.findMachine(m_ip);
            Machine m = new Machine(m_ip);

            m_holder.setMachine(m);
            report.getMachines().clear();

            if (machine != null) {
               report.addMachine(machine);
            }
         }

         super.visitEventReport(report);

         report.addMachine(m_holder.getMachine());
      }

      @Override
      public void visitMachine(Machine machine) {
         EventType type = machine.findType(m_type);

         machine.getTypes().clear();

         if (type != null) {
            EventType t = m_holder.getMachine().findOrCreateType(type.getId());

            m_holder.setType(t);
            m_helper.mergeType(t, type);
            machine.addType(type);
         }

         super.visitMachine(machine);
      }

      @Override
      public void visitType(EventType type) {
         EventType t = m_holder.getType();
         EventName n = t.findOrCreateName(Constants.ALL);

         for (EventName name : type.getNames().values()) {
            m_helper.mergeName(n, name);
            n.setSuccessMessageUrl(null);
            n.setFailMessageUrl(null);

            m_helper.mergeRanges(n.getRanges(), name.getRanges());
         }

         t.setSuccessMessageUrl(null);
         t.setFailMessageUrl(null);
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);
         type.getNames().clear();
      }
   }
}
