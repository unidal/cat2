package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.*;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventAllTypeGraphFilter.ID)
public class EventAllTypeGraphFilter implements ReportFilter<EventReport> {
    public static final String ID = "all-type-graph";

    @Inject
    private EventReportHelper m_helper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getReportName() {
        return EventConstants.NAME;
    }

    @Override
    public EventReport screen(RemoteContext ctx, EventReport report) {
        String type = ctx.getProperty("type", null);
        TypeGraphScreener visitor = new TypeGraphScreener(report.getDomain(), type);

        report.accept(visitor);
        return visitor.getReport();
    }

    @Override
    public void tailor(RemoteContext ctx, EventReport report) {
        String type = ctx.getProperty("type", null);
        String ip = ctx.getProperty("ip", null);
        TypeGraphTailor visitor = new TypeGraphTailor(ip, type);

        report.accept(visitor);
    }

    private class TypeGraphScreener extends BaseVisitor {
        private String m_type;

        private EventHolder m_holder = new EventHolder();

        public TypeGraphScreener(String domain, String type) {
            m_type = type;
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

            if (ta != null) {
                EventName na = ta.findOrCreateName(Constants.ALL);

                mergeType(ta, type);
                m_holder.setName(na);
            }

            for (EventName name : type.getNames().values()) {
                visitName(name);
            }
        }
    }

    private class TypeGraphTailor extends BaseVisitor {
        private String m_ip;

        private String m_type;

        private EventHolder m_holder = new EventHolder();

        public TypeGraphTailor(String ip, String type) {
            m_ip = ip;
            m_type = type;
        }

        @Override
        public void visitMachine(Machine machine) {
            EventType type = machine.findType(m_type);

            machine.getTypes().clear();

            if (type != null) {
                EventType t = m_holder.getMachine().findOrCreateType(type.getId());

                m_holder.setType(t);
                m_helper.mergeType(t, type);
                machine.addType(type);
            }

            super.visitMachine(machine);
        }

        @Override
        public void visitEventReport(EventReport report) {
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

            super.visitEventReport(report);

            report.addMachine(m_holder.getMachine());
        }

        @Override
        public void visitType(EventType type) {
            EventType t = m_holder.getType();
            EventName n = t.findOrCreateName(Constants.ALL);

            for (EventName name : type.getNames().values()) {
                m_helper.mergeName(n, name);
                n.setSuccessMessageUrl(null);
                n.setFailMessageUrl(null);

                m_helper.mergeRanges(n.getRanges(), name.getRanges());
            }

            t.setSuccessMessageUrl(null);
            t.setFailMessageUrl(null);
            type.setSuccessMessageUrl(null);
            type.setFailMessageUrl(null);
            type.getNames().clear();
        }

        @Override
        public void visitDistributionInType(DistributionInType distributionInType) {
            distributionInType.getDistributionInNames().clear();

            Bu bu = distributionInType.findBu(m_ip);

            distributionInType.getBus().clear();
            if (bu != null) {
                distributionInType.addBu(bu);
            }
        }
    }
}
