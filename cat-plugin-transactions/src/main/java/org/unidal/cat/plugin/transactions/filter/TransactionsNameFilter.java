package org.unidal.cat.plugin.transactions.filter;

import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.TransactionsHolder;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = TransactionsConstants.NAME + ":" + TransactionsNameFilter.ID)
public class TransactionsNameFilter implements ReportFilter<TransactionsReport> {
   public static final String ID = "name";

   @Inject
   private TransactionsHelper m_helper;

   @Inject
   private DomainGroupConfigService m_configService;

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getReportName() {
      return TransactionsConstants.NAME;
   }

   @Override
   public TransactionsReport screen(RemoteContext ctx, TransactionsReport report) {
      String group = ctx.getProperty("group", null);
      String bu = ctx.getProperty("bu", null);
      String type = ctx.getProperty("type", null);
      TypeScreener visitor = new TypeScreener(report.getDomain(), group, bu, type);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionsReport report) {
   }

   private class TypeScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private String m_type;

      private TransactionsHolder m_holder = new TransactionsHolder();

      public TypeScreener(String domain, String group, String bu, String type) {
         m_group = group;
         m_bu = bu;
         m_type = type;
         m_holder.setReport(new TransactionsReport());
      }

      public TransactionsReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitDepartment(TransactionsDepartment department) {
         TransactionsDepartment d = m_holder.getDepartment();
         TransactionsType t = d.findOrCreateType(m_type);
         TransactionsType type = department.findType(m_type);

         m_helper.mergeDepartment(d, department);
         m_holder.setType(t);

         if (type != null) {
            visitType(type);
         }
      }

      @Override
      public void visitName(TransactionsName name) {
         TransactionsName n = m_holder.getName();

         m_helper.mergeName(n, name);
      }

      @Override
      public void visitTransactionsReport(TransactionsReport report) {
         TransactionsReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_group != null) {
            // TODO department grouping?
         } else if (m_bu == null) {
            TransactionsDepartment d = r.findOrCreateDepartment(Constants.ALL);

            m_holder.setDepartment(d);

            for (TransactionsDepartment department : report.getDepartments().values()) {
               visitDepartment(department);
            }
         } else {
            TransactionsDepartment department = report.findOrCreateDepartment(m_bu);
            TransactionsDepartment d = r.findOrCreateDepartment(m_bu);

            if (department != null) {
               m_holder.setDepartment(d);
               visitDepartment(department);
            }
         }
      }

      @Override
      public void visitType(TransactionsType type) {
         TransactionsType t = m_holder.getType();

         m_helper.mergeType(t, type);

         for (TransactionsName name : type.getNames().values()) {
            TransactionsName n = t.findOrCreateName(name.getId());

            m_holder.setName(n);
            visitName(name);
         }
      }
   }
}
