package org.unidal.cat.plugin.transaction.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionTypeFilter.ID)
public class TransactionTypeFilter implements ReportFilter<TransactionReport> {
   public static final String ID = "type";

   @Inject
   private TransactionReportHelper m_helper;

   @Inject
   private DomainGroupConfigService m_configService;

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
      TypeScreener visitor = new TypeScreener(report.getDomain(), group, ip);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionReport report) {
      TypeTailor visitor = new TypeTailor();

      report.accept(visitor);
   }

   private class TypeScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private TransactionHolder m_holder = new TransactionHolder();

      public TypeScreener(String domain, String group, String ip) {
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

         m_helper.mergeMachine(m, machine);

         Collection<TransactionType> types = new ArrayList<TransactionType>(machine.getTypes().values());

         for (TransactionType type : types) {
            TransactionType t = m.findOrCreateType(type.getId());

            m_holder.setType(t);
            visitType(type);
         }
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
      }
   }

   private class TypeTailor extends BaseVisitor {
      @Override
      public void visitType(TransactionType type) {
         type.getNames().clear();
         type.getRange2s().clear();
         type.getAllDurations().clear();
      }
   }
}
