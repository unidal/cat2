package org.unidal.cat.plugin.transaction.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.cat.core.config.DomainConfigService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionNameFilter.ID)
public class TransactionNameFilter implements ReportFilter<TransactionReport> {
   public static final String ID = "name";

   @Inject
   private TransactionReportHelper m_helper;

   @Inject
   private DomainConfigService m_configService;

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
      String group = ctx.getProperty("group", null);
      String ip = ctx.getProperty("ip", null);
      String type = ctx.getProperty("type", null);
      NameScreener visitor = new NameScreener(report.getDomain(), group, ip, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionReport report) {
      NameTailor visitor = new NameTailor();

      report.accept(visitor);
   }

   private class NameScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private String m_type;

      private TransactionHolder m_holder = new TransactionHolder();

      public NameScreener(String domain, String group, String ip, String type) {
         m_type = type;
         m_group = group;
         m_ip = ip;
         m_holder.setReport(new TransactionReport(domain));
      }

      public TransactionReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine m = m_holder.getMachine();
         TransactionType t = m.findOrCreateType(m_type);
         TransactionType type = machine.findType(m_type);

         m_helper.mergeMachine(m, machine);
         m_holder.setType(t);

         if (type != null) {
            visitType(type);
         }
      }

      @Override
      public void visitName(TransactionName name) {
         TransactionName n = m_holder.getName();

         m_helper.mergeName(n, name);
      }

      @Override
      public void visitTransactionReport(TransactionReport report) {
         TransactionReport r = m_holder.getReport();

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
      public void visitType(TransactionType type) {
         TransactionType t = m_holder.getType();

         m_helper.mergeType(t, type);

         Collection<TransactionName> names = new ArrayList<TransactionName>(type.getNames().values());

         for (TransactionName name : names) {
            TransactionName n = t.findOrCreateName(name.getId());

            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameTailor extends BaseVisitor {
      @Override
      public void visitName(TransactionName name) {
         name.getRanges().clear();
         name.getDurations().clear();
         name.getAllDurations().clear();
      }

      @Override
      public void visitType(TransactionType type) {
         type.getRange2s().clear();
         type.getAllDurations().clear();
      }
   }
}
