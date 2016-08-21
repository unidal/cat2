package org.unidal.cat.plugin.transactions.filter;

import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.TransactionsHolder;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.BaseVisitor;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = TransactionsConstants.NAME + ":" + TransactionsTypeFilter.ID)
public class TransactionsTypeFilter implements ReportFilter<TransactionsReport> {
   public static final String ID = "type";

   @Inject
   private TransactionsHelper m_helper;

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
      TypeScreener visitor = new TypeScreener(report.getDomain(), group, bu);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, TransactionsReport report) {
   }

   private class TypeScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private TransactionsHolder m_holder = new TransactionsHolder();

      public TypeScreener(String domain, String group, String bu) {
         m_group = group;
         m_bu = bu;
         m_holder.setReport(new TransactionsReport());
      }

      public TransactionsReport getReport() {
         return m_holder.getReport();
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
      public void visitDepartment(TransactionsDepartment department) {
         TransactionsDepartment d = m_holder.getDepartment();

         m_helper.mergeDepartment(d, department);

         for (TransactionsType type : department.getTypes().values()) {
            TransactionsType t = d.findOrCreateType(type.getId());

            m_holder.setType(t);
            visitType(type);
         }
      }

      @Override
      public void visitType(TransactionsType type) {
         m_helper.mergeType(m_holder.getType(), type);
      }
   }
}
