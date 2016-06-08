package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.*;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventAllNameGraphFilter.ID)
public class EventAllNameGraphFilter implements ReportFilter<EventReport> {
    public static final String ID = "all-name-graph";

    @Inject
    private EventReportHelper m_helper;

    @Override
    public String getReportName() {
        return EventConstants.NAME;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public EventReport screen(RemoteContext ctx, EventReport report) {
        String type = ctx.getProperty("type", null);
        String name = ctx.getProperty("name", null);
        NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), type, name);

        report.accept(visitor);
        return visitor.getReport();
    }

    @Override
    public void tailor(RemoteContext ctx, EventReport report) {
        String type = ctx.getProperty("type", null);
        String name = ctx.getProperty("name", null);
        String ip = ctx.getProperty("ip", null);
        NameGraphTailor visitor = new NameGraphTailor(ip, type, name);

        report.accept(visitor);
    }

    private class NameGraphScreener extends BaseVisitor {
        private String m_type;

        private String m_name;

        private EventHolder m_holder = new EventHolder();

        public NameGraphScreener(String domain, String type, String name) {
            m_type = type;
            m_name = name;
            m_holder.setReport(new EventReport(domain));
        }

        public EventReport getReport() {
            return m_holder.getReport();
        }

        private void mergeName(EventName n, EventName name) {
            m_helper.mergeName(n, name);
            n.setSuccessMessageUrl(null);
            n.setFailMessageUrl(null);
        }

        private void mergeType(EventType t, EventType type) {
            m_helper.mergeType(t, type);
            t.setSuccessMessageUrl(null);
            t.setFailMessageUrl(null);
        }

        @Override
        public void visitMachine(Machine machine) {
            Machine machineAll = m_holder.getMachine();
            EventType type = machine.findType(m_type);

            if (machineAll != null) {
                m_helper.mergeMachine(machineAll, machine);

                if (type != null) {
                    EventType ta = machineAll.findOrCreateType(m_type);
                    m_holder.setType(ta);
                    visitType(type);
                }
            }
        }

        @Override
        public void visitName(EventName name) {
            EventName na = m_holder.getName();

            if (na != null) {
                mergeName(na, name);
                m_helper.mergeRanges(na.getRanges(), name.getRanges());
            }
        }

        @Override
        public void visitEventReport(EventReport report) {
            EventReport r = m_holder.getReport();

            m_helper.mergeReport(r, report);

            Machine machineAll = r.findOrCreateMachine(Constants.ALL);

            m_holder.setMachine(machineAll);

            for (Machine machine : report.getMachines().values()) {
                visitMachine(machine);
            }
        }

        @Override
        public void visitType(EventType type) {
            EventType ta = m_holder.getType();
            EventName name = type.findName(m_name);

            if (name != null && ta != null) {
                EventName na = ta.findOrCreateName(m_name);

                mergeType(ta, type);
                m_holder.setName(na);
                visitName(name);
            }
        }
    }

    private class NameGraphTailor extends BaseVisitor {
        private String m_ip;

        private String m_type;

        private String m_name;

        private EventHolder m_holder = new EventHolder();

        public NameGraphTailor(String ip, String type, String name) {
            m_ip = ip;
            m_type = type;
            m_name = name;
        }

        @Override
        public void visitMachine(Machine machine) {
            EventType type = machine.findType(m_type);

            machine.getTypes().clear();

            if (type != null) {
                EventType t = m_holder.getMachine().findOrCreateType(type.getId());

                machine.addType(type);
                m_helper.mergeType(t, type);
                m_holder.setType(t);
            }

            super.visitMachine(machine);
        }

        @Override
        public void visitEventReport(EventReport report) {
            boolean all = m_ip == null || m_ip.equals(Constants.ALL);

            if (all) {
                Machine machine = new Machine(Constants.ALL);

                m_holder.setMachine(machine);

                report.getDistributionInTypes().clear();
            } else {
                Machine machine = new Machine(m_ip);
                Machine m = report.findMachine(m_ip);

                report.getMachines().clear();
                if (null != m) {
                    report.addMachine(m);
                }
                m_holder.setMachine(machine);

                DistributionInType distributionInType = report.findOrCreateDistributionInType(m_type);
                report.getDistributionInTypes().clear();
                if (distributionInType != null) {
                    report.addDistributionInType(distributionInType);
                }
            }

            super.visitEventReport(report);

            report.addMachine(m_holder.getMachine());
        }

        @Override
        public void visitType(EventType type) {
            EventType t = m_holder.getType();
            EventName n = t.findOrCreateName(m_name);
            EventName name = type.findName(m_name);

            t.setSuccessMessageUrl(null);
            t.setFailMessageUrl(null);
            type.setSuccessMessageUrl(null);
            type.setFailMessageUrl(null);
            type.getNames().clear();

            if (name != null) {
                m_helper.mergeName(n, name);
                n.setSuccessMessageUrl(null);
                n.setFailMessageUrl(null);
                m_helper.mergeRanges(n.getRanges(), name.getRanges());

                type.addName(name);
                name.getRanges().clear();
                name.setSuccessMessageUrl(null);
                name.setFailMessageUrl(null);
            }
        }

        @Override
        public void visitDistributionInType(DistributionInType DistributionInType) {
            DistributionInType.getBus().clear();

            DistributionInName distributionInName = DistributionInType.findDistributionInName(m_name);
            DistributionInType.getDistributionInNames().clear();

            if (distributionInName != null) {
                DistributionInType.addDistributionInName(distributionInName);
            }

            super.visitDistributionInType(DistributionInType);
        }

        @Override
        public void visitDistributionInName(DistributionInName DistributionInName) {
            Bu bu = DistributionInName.findBu(m_ip);

            DistributionInName.getBus().clear();
            if (bu != null) {
                DistributionInName.addBu(bu);
            }
        }
    }
}
