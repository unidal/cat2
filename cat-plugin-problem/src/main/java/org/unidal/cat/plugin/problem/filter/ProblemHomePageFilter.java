package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.plugin.problem.ProblemConstants;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportFilter.class, value = ProblemConstants.NAME + ":" + ProblemHomePageFilter.ID)
public class ProblemHomePageFilter implements ReportFilter<ProblemReport> {
   public static final String ID = "homepage";

   @Inject
   private ProblemReportHelper m_helper;

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public String getReportName() {
      return ProblemConstants.NAME;
   }

   @Override
   public ProblemReport screen(RemoteReportContext ctx, ProblemReport report) {
      String ip = ctx.getProperty("ip", null);
      HomePageScreener visitor = new HomePageScreener(report.getDomain(), ip);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteReportContext ctx, ProblemReport report) {
      String ip = ctx.getProperty("ip", null);
      HomePageTailor visitor = new HomePageTailor(ip);

      report.accept(visitor);
   }

   private class HomePageScreener extends BaseVisitor {
      private String m_ip;

      private ProblemHolder m_holder = new ProblemHolder();

      public HomePageScreener(String domain, String ip) {
         this.m_ip = ip;
         this.m_holder.setReport(new ProblemReport(domain));
      }

      public ProblemReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitEntity(Entity entity) {
         Entity e = m_holder.getEntity();
         m_helper.mergeEntity(e, entity);
         m_helper.mergeDurations(e.getDurations(), entity.getDurations());
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine m = m_holder.getMachine();

         m_helper.mergeMachine(m, machine);

         for (Entity entity : machine.getEntities().values()) {
            Entity e = m.findOrCreateEntity(entity.getId());

            m_holder.setEntity(e);
            visitEntity(entity);
         }
      }

      @Override
      public void visitProblemReport(ProblemReport problemReport) {
         ProblemReport r = m_holder.getReport();

         m_helper.mergeReport(r, problemReport);

         if (m_ip == null || m_ip.equals(Constants.ALL)) {
            Machine m = r.findOrCreateMachine(Constants.ALL);

            m_holder.setMachine(m);

            for (Machine machine : problemReport.getMachines().values()) {
               visitMachine(machine);
            }
         } else {
            Machine machine = problemReport.findMachine(m_ip);
            Machine m = r.findOrCreateMachine(m_ip);

            if (machine != null) {
               m_holder.setMachine(m);
               visitMachine(machine);
            }
         }
      }
   }

   private class HomePageTailor extends BaseVisitor {
      private String m_ip;

      private ProblemHolder m_holder = new ProblemHolder();

      public HomePageTailor(String ip) {
         m_ip = ip;
      }

      @Override
      public void visitEntity(Entity entity) {
         Entity e = m_holder.getMachine().findOrCreateEntity(entity.getId());
         m_helper.mergeEntity(e, entity);
         m_helper.mergeDurations(e.getDurations(), entity.getDurations());
      }

      @Override
      public void visitProblemReport(ProblemReport problemReport) {
         boolean all = m_ip == null || m_ip.equals(Constants.ALL);

         if (all) {
            Machine machine = new Machine(Constants.ALL);
            m_holder.setMachine(machine);
         } else {
            Machine machine = new Machine(m_ip);
            m_holder.setMachine(machine);

            Machine m = problemReport.findMachine(m_ip);
            if (m != null) {
               problemReport.addMachine(m);
            }
         }

         super.visitProblemReport(problemReport);

         problemReport.getMachines().clear();
         problemReport.addMachine(m_holder.getMachine());
      }
   }
}
