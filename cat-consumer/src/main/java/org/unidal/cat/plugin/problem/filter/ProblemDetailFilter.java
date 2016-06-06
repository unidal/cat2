package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.*;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import org.unidal.cat.plugin.problem.ProblemConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.List;

@Named(type = ReportFilter.class, value = ProblemConstants.NAME + ":" + ProblemDetailFilter.ID)
public class ProblemDetailFilter implements ReportFilter<ProblemReport> {
    public static final String ID = "detail";

    @Inject
    private ProblemReportHelper m_helper;

    @Override
    public String getReportName() {
        return ProblemConstants.NAME;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ProblemReport screen(RemoteContext ctx, ProblemReport report) {
        String ip = ctx.getProperty("ip", null);
        String group = ctx.getProperty("group", null);
        ThreadScreener visitor = new ThreadScreener(report.getDomain(), ip, group);

        report.accept(visitor);
        return visitor.getReport();
    }

    @Override
    public void tailor(RemoteContext ctx, ProblemReport report) {
        String ip = ctx.getProperty("ip", null);
        String group = ctx.getProperty("group", null);
        ThreadTailor visitor = new ThreadTailor(ip, group);

        report.accept(visitor);
    }

    private class ThreadScreener extends BaseVisitor {
        private String m_ip;

        private String m_group;

        private ProblemHolder m_holder = new ProblemHolder();

        public ThreadScreener(String domain, String ip, String group) {
            this.m_ip = ip;
            this.m_group = group;
            this.m_holder.setReport(new ProblemReport(domain));
        }

        public ProblemReport getReport() {
            return m_holder.getReport();
        }

        @Override
        public void visitProblemReport(ProblemReport problemReport) {
            ProblemReport r = m_holder.getReport();

            m_helper.mergeReport(r, problemReport);

            if (m_ip != null && !m_ip.equals(Constants.ALL)) {
                Machine machine = problemReport.findMachine(m_ip);

                if (machine != null) {
                    Machine m = r.findOrCreateMachine(m_ip);
                    m_holder.setMachine(m);

                    visitMachine(machine);
                }
            }
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
        public void visitEntity(Entity entity) {
            Entity e = m_holder.getEntity();

            m_helper.mergeEntity(e, entity);

            for (JavaThread javaThread : entity.getThreads().values()) {
                if (javaThread.getGroupName().equals(m_group)) {
                    JavaThread t = e.findOrCreateThread(javaThread.getId());
                    m_holder.setJavaThread(t);

                    visitThread(javaThread);
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

    private class ThreadTailor extends BaseVisitor {
        private String m_ip;

        private String m_group;

        public ThreadTailor(String ip, String group) {
            m_ip = ip;
            m_group = group;
        }

        @Override
        public void visitProblemReport(ProblemReport problemReport) {
            if (m_ip != null && !m_ip.equals(Constants.ALL)) {
                Machine m = problemReport.findMachine(m_ip);

                problemReport.getMachines().clear();
                if (m != null) {
                    problemReport.addMachine(m);
                }
            } else {
                problemReport.getMachines().clear();
            }

            super.visitProblemReport(problemReport);
        }

        @Override
        public void visitEntity(Entity entity) {
            entity.getDurations().clear();

            List<String> deleteThreads = new ArrayList<String>();

            for (JavaThread javaThread : entity.getThreads().values()) {
                if (!javaThread.getGroupName().equals(m_group)) {
                    deleteThreads.add(javaThread.getId());
                }
            }

            for (String id : deleteThreads) {
                entity.getThreads().remove(id);
            }

            super.visitEntity(entity);
        }

        @Override
        public void visitSegment(Segment segment) {
            segment.getMessages().clear();
        }
    }
}
