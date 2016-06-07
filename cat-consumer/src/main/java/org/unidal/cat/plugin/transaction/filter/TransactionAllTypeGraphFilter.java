package org.unidal.cat.plugin.transaction.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionAllTypeGraphFilter.ID)
public class TransactionAllTypeGraphFilter implements ReportFilter<TransactionReport> {
    public static final String ID = "all-type-graph";

    @Inject
    private TransactionReportHelper m_helper;

    @Inject
    private ProjectService m_projectService;

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
        String ip = ctx.getProperty("ip", null);
        String type = ctx.getProperty("type", null);
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
        private String m_type;

        private String m_ip;

        private TransactionHolder m_holder = new TransactionHolder();

        public TypeGraphScreener(String domain, String ip,  String type) {
            m_ip = ip;
            m_type = type;
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
            Machine machineAll = m_holder.getMachine();
            TransactionType type = machine.findType(m_type);

            if (machineAll != null) {
                m_helper.mergeMachine(machineAll, machine);

                if (type != null) {
                    TransactionType ta = machineAll.findOrCreateType(m_type);

                    m_holder.setType(ta);
                    visitType(type);
                }
            }
        }

        @Override
        public void visitName(TransactionName name) {
            TransactionName na = m_holder.getName();

            if (na != null) {
                mergeName(na, name);

                m_helper.mergeDurations(na.getDurations(), name.getDurations());
                m_helper.mergeRanges(na.getRanges(), name.getRanges());
            }
        }

        @Override
        public void visitTransactionReport(TransactionReport report) {
            TransactionReport r = m_holder.getReport();

            m_helper.mergeReport(r, report);

            if (m_ip != null && !m_ip.equals(Constants.ALL) && !m_ip.equals(m_projectService.findBu(report.getDomain()))) {
                return;
            }

            Machine machineAll = r.findOrCreateMachine(Constants.ALL);

            m_holder.setMachine(machineAll);

            for (Machine machine : report.getMachines().values()) {
                visitMachine(machine);
            }
        }

        @Override
        public void visitType(TransactionType type) {
            TransactionType ta = m_holder.getType();

            if (ta != null) {
                TransactionName na = ta.findOrCreateName(Constants.ALL);

                mergeType(ta, type);
                m_holder.setName(na);
            }

            for (TransactionName name : type.getNames().values()) {
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

                report.getDistributionInTypes().clear();
            } else {
                Machine machine = report.findMachine(m_ip);
                Machine m = new Machine(m_ip);

                m_holder.setMachine(m);
                report.getMachines().clear();

                if (machine != null) {
                    report.addMachine(machine);
                }

                DistributionInType distributionInType = report.findOrCreateDistributionInType(m_type);
                report.getDistributionInTypes().clear();
                if (distributionInType != null) {
                    report.addDistributionInType(distributionInType);
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

        @Override
        public void visitDistributionInType(DistributionInType distributionInType) {
            distributionInType.getDistributionInName().clear();

            Bu bu = distributionInType.findBu(m_ip);

            distributionInType.getBus().clear();
            if (bu != null) {
                distributionInType.addBu(bu);
            }
        }
    }
}
