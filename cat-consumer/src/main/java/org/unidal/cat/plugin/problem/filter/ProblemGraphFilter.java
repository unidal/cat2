package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import org.unidal.cat.plugin.problem.ProblemConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.List;

@Named(type = ReportFilter.class, value = ProblemConstants.NAME + ":" + ProblemGraphFilter.ID)
public class ProblemGraphFilter implements ReportFilter<ProblemReport> {
   public static final String ID = "graph";

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
   public ProblemReport screen(RemoteContext ctx, ProblemReport report) {
      String ip = ctx.getProperty("ip", null);
      String type = ctx.getProperty("type", null);
      String status = ctx.getProperty("status", null);
      TypeScreener visitor = new TypeScreener(report.getDomain(), ip, type, status);

      report.accept(visitor);
      return visitor.getReport();
   }

   @Override
   public void tailor(RemoteContext ctx, ProblemReport report) {
      String ip = ctx.getProperty("ip", null);
      String type = ctx.getProperty("type", null);
      String status = ctx.getProperty("status", null);
      TypeTailor visitor = new TypeTailor(ip, type, status);

      report.accept(visitor);
   }

   private class TypeScreener extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private String m_status;

      private ProblemHolder m_holder = new ProblemHolder();

      public TypeScreener(String domain, String ip, String type, String status) {
         this.m_ip = ip;
         this.m_type = type;
         this.m_status = status;
         this.m_holder.setReport(new ProblemReport(domain));
      }

      public ProblemReport getReport() {
         return m_holder.getReport();
      }

      @Override
      public void visitEntity(Entity entity) {
         Entity e = m_holder.getEntity();

         m_helper.mergeEntity(e, entity);

         for (JavaThread javaThread : entity.getThreads().values()) {
            JavaThread t = e.findOrCreateThread(javaThread.getId());
            m_holder.setJavaThread(t);

            visitThread(javaThread);
         }

         m_helper.mergeDurations(e.getDurations(), entity.getDurations());
      }

      @Override
      public void visitMachine(Machine machine) {
         Machine m = m_holder.getMachine();

         m_helper.mergeMachine(m, machine);

         for (Entity entity : machine.getEntities().values()) {
            if (entity.getType().equals(m_type) && (m_status == null || entity.getStatus().equals(m_status))) {
               Entity e = m.findOrCreateEntity(entity.getId());

               m_holder.setEntity(e);
               visitEntity(entity);
            }
         }
      }

      @Override
      public void visitProblemReport(ProblemReport problemReport) {
         ProblemReport r = m_holder.getReport();

         m_helper.mergeReport(r, problemReport);

         if (m_ip == null || m_ip.equals(Constants.ALL)) {
            for (Machine machine : problemReport.getMachines().values()) {
               Machine m = r.findOrCreateMachine(machine.getIp());
               m_holder.setMachine(m);

               visitMachine(machine);
            }
         } else {
            Machine machine = problemReport.findMachine(m_ip);

            if (machine != null) {
               Machine m = r.findOrCreateMachine(m_ip);
               m_holder.setMachine(m);

               visitMachine(machine);
            }
         }
      }

      @Override
      public void visitThread(JavaThread thread) {
         JavaThread t = m_holder.getJavaThread();
         m_helper.mergeJavaThread(t, thread);
         m_helper.mergeSegments(t.getSegments(), thread.getSegments());
      }
   }

   private class TypeTailor extends BaseVisitor {
      private String m_ip;

      private String m_type;

      private String m_status;

      private boolean m_all;

      public TypeTailor(String ip, String type, String status) {
         m_ip = ip;
         m_type = type;
         m_status = status;
         m_all = m_ip == null || m_ip.equals(Constants.ALL);
      }

      @Override
      public void visitDuration(Duration duration) {
         duration.getMessages().clear();
      }

      @Override
      public void visitEntity(Entity entity) {
         if (!m_all) {
            entity.getDurations().clear();
         }

         super.visitEntity(entity);
      }

      @Override
      public void visitMachine(Machine machine) {
         List<String> filteredId = new ArrayList<String>();

         for (Entity entity : machine.getEntities().values()) {
            if (!(entity.getType().equals(m_type) && (m_status == null || entity.getStatus().equals(m_status)))) {
               filteredId.add(entity.getId());
            }
         }

         for (String id : filteredId) {
            machine.getEntities().remove(id);
         }

         super.visitMachine(machine);
      }

      @Override
      public void visitProblemReport(ProblemReport problemReport) {
         if (!m_all) {
            Machine m = problemReport.findMachine(m_ip);

            problemReport.getMachines().clear();
            if (m != null) {
               problemReport.addMachine(m);
            }
         }

         super.visitProblemReport(problemReport);
      }

      @Override
      public void visitSegment(Segment segment) {
         segment.getMessages().clear();
      }
   }
}
