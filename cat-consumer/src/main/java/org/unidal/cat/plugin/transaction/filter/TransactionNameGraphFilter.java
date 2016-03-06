package org.unidal.cat.plugin.transaction.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionNameGraphFilter.ID)
public class TransactionNameGraphFilter implements ReportFilter<TransactionReport> {
	public static final String ID = "name-graph";

	@Inject
	private TransactionReportHelper m_helper;

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
		String type = ctx.getProperty("type", null);
		String name = ctx.getProperty("name", null);
		String ip = ctx.getProperty("ip", null);
		NameGraphScreener visitor = new NameGraphScreener(report.getDomain(), ip, type, name);

		report.accept(visitor);
		return visitor.getReport();
	}

	@Override
	public void tailor(RemoteContext ctx, TransactionReport report) {
		String type = ctx.getProperty("type", null);
		String name = ctx.getProperty("name", null);
		String ip = ctx.getProperty("ip", null);
		NameGraphTailor visitor = new NameGraphTailor(ip, type, name);

		report.accept(visitor);
	}

	private class NameGraphScreener extends BaseVisitor {
		private String m_ip;

		private String m_type;

		private String m_name;

		private TransactionHolder m_holder = new TransactionHolder();

		private TransactionHolder m_all = new TransactionHolder();

		public NameGraphScreener(String domain, String ip, String type, String name) {
			m_type = type;
			m_name = name;
			m_ip = ip;
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
			Machine machineAll = m_all.getMachine();
			Machine m = m_holder.getMachine();
			TransactionType type = machine.findType(m_type);

			if (machineAll != null) {
				m_helper.mergeMachine(machineAll, machine);
				m_helper.mergeMachine(m, machine);

				if (type != null) {
					TransactionType ta = machineAll.findOrCreateType(m_type);
					TransactionType t = m.findOrCreateType(m_type);

					m_all.setType(ta);
					m_holder.setType(t);
					visitType(type);
				}
			} else {
				m_helper.mergeMachine(m, machine);

				if (type != null) {
					TransactionType t = m.findOrCreateType(m_type);

					m_holder.setType(t);
					visitType(type);
				}
			}
		}

		@Override
		public void visitName(TransactionName name) {
			TransactionName na = m_all.getName();
			TransactionName n = m_holder.getName();

			if (na != null) {
				mergeName(n, name);

				n = na;
			}

			mergeName(n, name);

			m_helper.mergeDurations(n.getDurations(), name.getDurations());
			m_helper.mergeRanges(n.getRanges(), name.getRanges());
		}

		@Override
		public void visitTransactionReport(TransactionReport report) {
			TransactionReport r = m_holder.getReport();

			m_helper.mergeReport(r, report);

			if (m_ip == null || m_ip.equals(Constants.ALL)) {
				Collection<Machine> machines = new ArrayList<Machine>(report.getMachines().values());

				for (Machine machine : machines) {
					r.findOrCreateMachine(machine.getIp());
				}

				Machine machineAll = r.findOrCreateMachine(Constants.ALL);

				m_all.setMachine(machineAll);

				for (Machine machine : machines) {
					Machine m = r.findMachine(machine.getIp());

					m_holder.setMachine(m);
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
		public void visitType(TransactionType type) {
			TransactionType ta = m_all.getType();
			TransactionType t = m_holder.getType();
			TransactionName name = type.findName(m_name);

			if (name != null) {
				if (ta != null) {
					TransactionName na = ta.findOrCreateName(m_name);

					mergeType(ta, type);
					m_all.setName(na);
				}

				TransactionName n = t.findOrCreateName(m_name);

				mergeType(t, type);
				m_holder.setName(n);
				visitName(name);
			}
		}
	}

	private class NameGraphTailor extends BaseVisitor {
		private String m_ip;

		private String m_type;

		private String m_name;

		private TransactionHolder m_holder = new TransactionHolder();

		public NameGraphTailor(String ip, String type, String name) {
			m_ip = ip;
			m_type = type;
			m_name = name;
		}

		@Override
		public void visitMachine(Machine machine) {
			TransactionType type = machine.findType(m_type);

			machine.getTypes().clear();

			if (type != null) {
				TransactionType t = m_holder.getMachine().findOrCreateType(type.getId());

				machine.addType(type);
				m_helper.mergeType(t, type);
				m_holder.setType(t);
			}

			super.visitMachine(machine);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			boolean all = m_ip == null || m_ip.equals(Constants.ALL);

			if (all) {
				Machine machine = new Machine(Constants.ALL);

				m_holder.setMachine(machine);
			} else {
				Machine machine = new Machine(m_ip);
				Machine m = transactionReport.findMachine(m_ip);

				transactionReport.getMachines().clear();
				transactionReport.addMachine(m);
				m_holder.setMachine(machine);
			}

			super.visitTransactionReport(transactionReport);

			transactionReport.addMachine(m_holder.getMachine());
		}

		@Override
		public void visitType(TransactionType type) {
			TransactionType t = m_holder.getType();
			TransactionName n = t.findOrCreateName(m_name);
			TransactionName name = type.findName(m_name);

			if (name != null) {
				m_helper.mergeName(n, name);
				n.setSuccessMessageUrl(null);
				n.setFailMessageUrl(null);
				m_helper.mergeDurations(n.getDurations(), name.getDurations());
				m_helper.mergeRanges(n.getRanges(), name.getRanges());
			}

			t.setSuccessMessageUrl(null);
			t.setFailMessageUrl(null);
			type.setSuccessMessageUrl(null);
			type.setFailMessageUrl(null);
			type.getNames().clear();
			type.addName(name);

			name.getRanges().clear();
			name.getDurations().clear();
			name.setSuccessMessageUrl(null);
			name.setFailMessageUrl(null);
		}
	}
}
