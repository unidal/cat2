package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.Collection;

@Named(type = ReportFilter.class, value = EventConstants.NAME + ":" + EventNameFilter.ID)
public class EventNameFilter implements ReportFilter<EventReport> {
	public static final String ID = "name";

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
		String ip = ctx.getProperty("ip", null);
		NameScreener visitor = new NameScreener(report.getDomain(), type, ip);

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

		public NameScreener(String domain, String type, String ip) {
			m_typeName = type;
			m_ip = ip;
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

			if (m_ip == null || m_ip.equals(Constants.ALL)) {
				Machine m = r.findOrCreateMachine(Constants.ALL);
				Collection<Machine> machines = new ArrayList<Machine>(report.getMachines().values());

				m_holder.setMachine(m);

				for (Machine machine : machines) {
					visitMachine(machine);
				}
			} else {
				Machine machine = report.findMachine(m_ip);
				Machine m = r.findOrCreateMachine(m_ip);

				if (machine != null) {
					m_holder.setMachine(m);
					visitMachine(machine);
				}
			}
		}

		@Override
		public void visitType(EventType type) {
			EventType t = m_holder.getType();

			m_helper.mergeType(t, type);

			Collection<EventName> names = new ArrayList<EventName>(type.getNames().values());

			for (EventName name : names) {
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
		public void visitEventReport(EventReport eventReport) {
			boolean all = m_ip == null || m_ip.equals(Constants.ALL);

			if (all) {
				m_machine = new Machine(Constants.ALL);
			} else {
				m_machine = new Machine(m_ip);

				Machine m = eventReport.findMachine(m_ip);
				eventReport.getMachines().clear();
				eventReport.addMachine(m);
			}

			super.visitEventReport(eventReport);

			eventReport.getMachines().clear();
			eventReport.addMachine(m_machine);
		}

		@Override
		public void visitType(EventType type) {
			super.visitType(type);
		}
	}
}
