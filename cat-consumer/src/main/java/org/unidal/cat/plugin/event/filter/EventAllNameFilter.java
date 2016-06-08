package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventAllNameFilter.ID)
public class EventAllNameFilter implements ReportFilter<EventReport> {
    public static final String ID = "all-name";

    @Inject
    private EventReportHelper m_helper;

    @Inject
    private ProjectService m_projectService;

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
        String ip = ctx.getProperty("ip", null);
        NameScreener visitor = new NameScreener(report.getDomain(), ip, type);

        report.accept(visitor);
        return visitor.getReport();
    }

    @Override
    public void tailor(RemoteContext ctx, EventReport report) {
        String type = ctx.getProperty("type", null);
        String ip = ctx.getProperty("ip", null);
        NameTailor visitor = new NameTailor(type, ip);

        report.accept(visitor);
    }

    private class NameScreener extends BaseVisitor {
        private String m_typeName;

        private String m_ip;

        private EventHolder m_holder = new EventHolder();

        public NameScreener(String domain, String ip, String type) {
            m_ip = ip;
            m_typeName = type;
            m_holder.setReport(new EventReport(domain));
        }

        public EventReport getReport() {
            return m_holder.getReport();
        }

        @Override
        public void visitMachine(Machine machine) {
            Machine m = m_holder.getMachine();
            EventType t = m.findOrCreateType(m_typeName);
            EventType type = machine.findType(m_typeName);

            m_helper.mergeMachine(m, machine);
            m_holder.setType(t);

            if (type != null) {
                visitType(type);
            }
        }

        @Override
        public void visitName(EventName name) {
            EventName n = m_holder.getName();

            m_helper.mergeName(n, name);
        }

        @Override
        public void visitEventReport(EventReport report) {
            EventReport r = m_holder.getReport();

            m_helper.mergeReport(r, report);

            if (m_ip != null && !m_ip.equals(Constants.ALL) && !m_ip.equals(m_projectService.findBu(report.getDomain()))) {
                return;
            }

            Machine m = r.findOrCreateMachine(Constants.ALL);

            m_holder.setMachine(m);

            for (Machine machine : report.getMachines().values()) {
                visitMachine(machine);
            }
        }

        @Override
        public void visitType(EventType type) {
            EventType t = m_holder.getType();

            m_helper.mergeType(t, type);

            for (EventName name : type.getNames().values()) {
                EventName n = t.findOrCreateName(name.getId());

                m_holder.setName(n);
                visitName(name);
            }
        }
    }

    private class NameTailor extends BaseVisitor {
        private String m_typeName;

        private String m_ip;

        private Machine m_machine;

        private EventType m_type;

        public NameTailor(String type, String ip) {
            m_typeName = type;
            m_ip = ip;
        }

        @Override
        public void visitMachine(Machine machine) {
            EventType type = machine.findType(m_typeName);

            machine.getTypes().clear();

            if (type != null) {
                m_type = m_machine.findOrCreateType(type.getId());

                m_helper.mergeType(m_type, type);
                machine.addType(type);
            }

            super.visitMachine(machine);
        }

        @Override
        public void visitName(EventName name) {
            name.getRanges().clear();

            EventName n = m_type.findOrCreateName(name.getId());

            m_helper.mergeName(n, name);
        }

        @Override
        public void visitEventReport(EventReport EventReport) {
            boolean all = m_ip == null || m_ip.equals(Constants.ALL);

            if (all) {
                m_machine = new Machine(Constants.ALL);
            } else {
                m_machine = new Machine(m_ip);

                Machine m = EventReport.findMachine(m_ip);
                EventReport.getMachines().clear();

                if (m != null) {
                    EventReport.addMachine(m);
                }
            }

            super.visitEventReport(EventReport);

            EventReport.getMachines().clear();
            EventReport.addMachine(m_machine);
            EventReport.getDistributionInTypes().clear();
        }

        @Override
        public void visitType(EventType type) {
            super.visitType(type);
        }
    }
}
