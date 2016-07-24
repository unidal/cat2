package org.unidal.cat.plugin.transaction.filter;

import com.dianping.cat.Constants;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.Collection;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionTypeGraphFilter.ID)
public class TransactionTypeGraphFilter implements ReportFilter<TransactionReport> {
   public static final String ID = "type-graph";

   @Inject
   private TransactionReportHelper m_helper;

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getReportName() {
      return TransactionConstants.NAME;
   }

   @Override
   public TransactionReport screen(RemoteContext ctx, TransactionReport report) {
      String type = ctx.getProperty("type", null);
      String ip = ctx.getProperty("ip", null);
      TypeGraphScreener visitor = new TypeGraphScreener(report.getDomain(), ip, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionReport report) {
      String type = ctx.getProperty("type", null);
      String ip = ctx.getProperty("ip", null);
      TypeGraphTailor visitor = new TypeGraphTailor(ip, type);

      report.accept(visitor);
   }

   private class TypeGraphScreener extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private TransactionHolder m_holder = new TransactionHolder();

      private TransactionHolder m_all = new TransactionHolder();

      public TypeGraphScreener(String domain, String ip, String type) {
         m_type = type;
         m_ip = ip;
         m_holder.setReport(new TransactionReport(domain));
      }

      public TransactionReport getReport() {
         return m_holder.getReport();
      }

      private void mergeName(TransactionName n, TransactionName name) {
         m_helper.mergeName(n, name);
         n.setSuccessMessageUrl(null);
         n.setFailMessageUrl(null);
      }

      private void mergeType(TransactionType t, TransactionType type) {
         m_helper.mergeType(t, type);
         t.setSuccessMessageUrl(null);
         t.setFailMessageUrl(null);
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine machineAll = m_all.getMachine();
         Machine m = m_holder.getMachine();
         TransactionType type = machine.findType(m_type);

         if (machineAll != null) {
            m_helper.mergeMachine(machineAll, machine);
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               TransactionType ta = machineAll.findOrCreateType(m_type);
               TransactionType t = m.findOrCreateType(m_type);

               m_all.setType(ta);
               m_holder.setType(t);
               visitType(type);
            }
         } else {
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               TransactionType t = m.findOrCreateType(m_type);

               m_holder.setType(t);
               visitType(type);
            }
         }
      }

      @Override
      public void visitName(TransactionName name) {
         TransactionName na = m_all.getName();
         TransactionName n = m_holder.getName();

         if (na != null) {
            mergeName(na, name);

            m_helper.mergeDurations(na.getDurations(), name.getDurations());
            m_helper.mergeRanges(na.getRanges(), name.getRanges());
         } else {
            mergeName(n, name);

            m_helper.mergeDurations(n.getDurations(), name.getDurations());
            m_helper.mergeRanges(n.getRanges(), name.getRanges());
         }
      }

      @Override
      public void visitTransactionReport(TransactionReport report) {
         TransactionReport r = m_holder.getReport();

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
      public void visitType(TransactionType type) {
         TransactionType ta = m_all.getType();
         TransactionType t = m_holder.getType();

         if (ta != null) {
            TransactionName na = ta.findOrCreateName(Constants.ALL);

            mergeType(ta, type);
            m_all.setName(na);
         } else {
            TransactionName n = t.findOrCreateName(Constants.ALL);

            m_holder.setName(n);
         }

         mergeType(t, type);

         Collection<TransactionName> names = new ArrayList<TransactionName>(type.getNames().values());

         for (TransactionName name : names) {
            visitName(name);
         }
      }
   }

   private class TypeGraphTailor extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private TransactionHolder m_holder = new TransactionHolder();

      public TypeGraphTailor(String ip, String type) {
         m_ip = ip;
         m_type = type;
      }

      @Override
      public void visitMachine(Machine machine) {
         TransactionType type = machine.findType(m_type);

         machine.getTypes().clear();

         if (type != null) {
            TransactionType t = m_holder.getMachine().findOrCreateType(type.getId());

            m_holder.setType(t);
            m_helper.mergeType(t, type);
            machine.addType(type);
         }

         super.visitMachine(machine);
      }

      @Override
      public void visitTransactionReport(TransactionReport report) {
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

         super.visitTransactionReport(report);

         report.addMachine(m_holder.getMachine());
      }

      @Override
      public void visitType(TransactionType type) {
         TransactionType t = m_holder.getType();
         TransactionName n = t.findOrCreateName(Constants.ALL);

         for (TransactionName name : type.getNames().values()) {
            m_helper.mergeName(n, name);
            n.setSuccessMessageUrl(null);
            n.setFailMessageUrl(null);
            n.setSlowestMessageUrl(null);

            m_helper.mergeDurations(n.getDurations(), name.getDurations());
            m_helper.mergeRanges(n.getRanges(), name.getRanges());
         }

         t.setSuccessMessageUrl(null);
         t.setFailMessageUrl(null);
         t.setSlowestMessageUrl(null);
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);
         type.setSlowestMessageUrl(null);
         type.getNames().clear();
      }
   }
}
