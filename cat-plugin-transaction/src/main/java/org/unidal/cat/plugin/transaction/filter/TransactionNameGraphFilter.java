package org.unidal.cat.plugin.transaction.filter;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
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

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionNameGraphFilter.ID)
public class TransactionNameGraphFilter implements ReportFilter<TransactionReport> {
   public static final String ID = "name-graph";

   @Inject
   private TransactionHelper m_helper;

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
      String type = ctx.getProperty("type", null);
      String name = ctx.getProperty("name", null);
      NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), group, ip, type, name);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionReport report) {
      NameGraphTailor visitor = new NameGraphTailor();

      report.accept(visitor);
   }

   private class NameGraphScreener extends BaseVisitor {
      private String m_group;

      private String m_ip;

      private String m_type;

      private String m_name;

      private TransactionHolder m_holder = new TransactionHolder();

      private TransactionHolder m_all = new TransactionHolder();

      public NameGraphScreener(String domain, String group, String ip, String type, String name) {
         m_group = group;
         m_ip = ip;
         m_type = type;
         m_name = name;
         m_holder.setReport(new TransactionReport(domain));
      }

      public TransactionReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine all = m_all.getMachine();
         Machine m = m_holder.getMachine();
         TransactionType type = machine.findType(m_type);

         if (all != null) {
            m_helper.mergeMachine(all, machine);
            m_helper.mergeMachine(m, machine);

            if (type != null) {
               TransactionType ta = all.findOrCreateType(m_type);
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
            m_helper.mergeName(n, name);

            n = na;
         }

         m_helper.mergeName(n, name);

         m_helper.mergeDurations(n.getDurations(), name.getDurations());
         m_helper.mergeRanges(n.getRanges(), name.getRanges());
      }

      @Override
      public void visitTransactionReport(TransactionReport report) {
         TransactionReport r = m_holder.getReport();
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
      public void visitType(TransactionType type) {
         TransactionType ta = m_all.getType();
         TransactionType t = m_holder.getType();
         TransactionName name = type.findName(m_name);

         if (name != null) {
            if (ta != null) {
               TransactionName na = ta.findOrCreateName(m_name);

               m_helper.mergeType(ta, type);
               m_all.setName(na);
            }

            TransactionName n = t.findOrCreateName(m_name);

            m_helper.mergeType(t, type);
            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameGraphTailor extends BaseVisitor {
      @Override
      public void visitName(TransactionName name) {
         name.setSuccessMessageUrl(null);
         name.setFailMessageUrl(null);
         name.setSlowestMessageUrl(null);

         super.visitName(name);
      }

      @Override
      public void visitType(TransactionType type) {
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);
         type.setSlowestMessageUrl(null);

         super.visitType(type);
      }
   }
}
