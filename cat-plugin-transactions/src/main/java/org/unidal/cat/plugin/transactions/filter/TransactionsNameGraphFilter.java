package org.unidal.cat.plugin.transactions.filter;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.TransactionsHolder;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.BaseVisitor;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;

@Named(type = ReportFilter.class, value = TransactionsConstants.NAME + ":" + TransactionsNameGraphFilter.ID)
public class TransactionsNameGraphFilter implements ReportFilter<TransactionsReport> {
   public static final String ID = "name-graph";

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
   public TransactionsReport screen(RemoteReportContext ctx, TransactionsReport report) {
      String group = ctx.getProperty("group", null);
      String bu = ctx.getProperty("bu", null);
      String type = ctx.getProperty("type", null);
      String name = ctx.getProperty("name", null);
      NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), group, bu, type, name);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteReportContext ctx, TransactionsReport report) {
      NameGraphTailor visitor = new NameGraphTailor();

      report.accept(visitor);
   }

   private class NameGraphScreener extends BaseVisitor {
      private String m_group;

      private String m_bu;

      private String m_type;

      private String m_name;

      private TransactionsHolder m_holder = new TransactionsHolder();

      private TransactionsHolder m_all = new TransactionsHolder();

      public NameGraphScreener(String domain, String group, String bu, String type, String name) {
         m_group = group;
         m_bu = bu;
         m_type = type;
         m_name = name;
         m_holder.setReport(new TransactionsReport());
      }

      public TransactionsReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitDepartment(TransactionsDepartment department) {
         TransactionsDepartment all = m_all.getDepartment();
         TransactionsDepartment m = m_holder.getDepartment();
         TransactionsType type = department.findType(m_type);

         if (all != null) {
            m_helper.mergeDepartment(all, department);
            m_helper.mergeDepartment(m, department);

            if (type != null) {
               TransactionsType ta = all.findOrCreateType(m_type);
               TransactionsType t = m.findOrCreateType(m_type);

               m_all.setType(ta);
               m_holder.setType(t);
               visitType(type);
            }
         } else {
            m_helper.mergeDepartment(m, department);

            if (type != null) {
               TransactionsType t = m.findOrCreateType(m_type);

               m_holder.setType(t);
               visitType(type);
            }
         }
      }

      @Override
      public void visitName(TransactionsName name) {
         TransactionsName na = m_all.getName();
         TransactionsName n = m_holder.getName();

         if (na != null) {
            m_helper.mergeName(n, name);

            n = na;
         }

         m_helper.mergeName(n, name);

         m_helper.mergeDurations(n.getDurations(), name.getDurations());
         m_helper.mergeRanges(n.getRanges(), name.getRanges());
      }

      @Override
      public void visitTransactionsReport(TransactionsReport report) {
         TransactionsReport r = m_holder.getReport();

         m_helper.mergeReport(r, report);

         if (m_group != null) {
            String domain = report.getDomain();

            for (TransactionsDepartment department : report.getDepartments().values()) {
               if (m_configService.isInGroup(domain, m_group, department.getId())) {
                  r.findOrCreateDepartment(department.getId());
               }
            }

            TransactionsDepartment m = r.findOrCreateDepartment(m_group);

            m_all.setDepartment(m);

            for (TransactionsDepartment department : report.getDepartments().values()) {
               if (m_configService.isInGroup(domain, m_group, department.getId())) {
                  m_holder.setDepartment(r.findDepartment(department.getId()));
                  visitDepartment(department);
               }
            }
         } else if (m_bu == null || m_bu.equals(Constants.ALL)) {
            for (TransactionsDepartment department : report.getDepartments().values()) {
               r.findOrCreateDepartment(department.getId());
            }

            TransactionsDepartment m = r.findOrCreateDepartment(Constants.ALL);

            m_all.setDepartment(m);

            for (TransactionsDepartment department : report.getDepartments().values()) {
               m_holder.setDepartment(r.findDepartment(department.getId()));
               visitDepartment(department);
            }
         } else {
            TransactionsDepartment department = report.findDepartment(m_bu);
            TransactionsDepartment m = r.findOrCreateDepartment(m_bu);

            if (department != null) {
               m_holder.setDepartment(m);
               visitDepartment(department);
            }
         }
      }

      @Override
      public void visitType(TransactionsType type) {
         TransactionsType ta = m_all.getType();
         TransactionsType t = m_holder.getType();
         TransactionsName name = type.findName(m_name);

         if (name != null) {
            if (ta != null) {
               TransactionsName na = ta.findOrCreateName(m_name);

               m_helper.mergeType(ta, type);
               m_all.setName(na);
            }

            TransactionsName n = t.findOrCreateName(m_name);

            m_helper.mergeType(t, type);
            m_holder.setName(n);
            visitName(name);
         }
      }
   }

   private class NameGraphTailor extends BaseVisitor {
      @Override
      public void visitName(TransactionsName name) {
         name.setSuccessMessageUrl(null);
         name.setFailMessageUrl(null);
         name.setSlowestMessageUrl(null);

         super.visitName(name);
      }

      @Override
      public void visitType(TransactionsType type) {
         type.setSuccessMessageUrl(null);
         type.setFailMessageUrl(null);
         type.setSlowestMessageUrl(null);

         super.visitType(type);
      }
   }
}
