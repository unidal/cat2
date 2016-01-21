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

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionNameFilter.ID)
public class TransactionNameFilter implements ReportFilter<TransactionReport> {
	public static final String ID = "name";

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
		String ip = ctx.getProperty("ip", null);
		NameScreener visitor = new NameScreener(report.getDomain(), type, ip);

		report.accept(visitor);
		return visitor.getReport();
	}

	@Override
	public void tailor(RemoteContext ctx, TransactionReport report) {
		String type = ctx.getProperty("type", null);
		String ip = ctx.getProperty("ip", null);
		NameTailor visitor = new NameTailor(type, ip);

		report.accept(visitor);
	}

	private class NameScreener extends BaseVisitor {
		private String m_typeName;

		private String m_ip;

		private TransactionHolder m_holder = new TransactionHolder();

		public NameScreener(String domain, String type, String ip) {
			m_typeName = type;
			m_ip = ip;
			m_holder.setReport(new TransactionReport(domain));
		}

		public TransactionReport getReport() {
			return m_holder.getReport();
		}

		@Override
		public void visitMachine(Machine machine) {
			Machine m = m_holder.getMachine();
			TransactionType t = m.findOrCreateType(m_typeName);
			TransactionType type = machine.findType(m_typeName);

			m_helper.mergeMachine(m, machine);
			m_holder.setType(t);

			if (type != null) {
				visitType(type);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			TransactionName n = m_holder.getName();

			m_helper.mergeName(n, name);
		}

		@Override
		public void visitTransactionReport(TransactionReport report) {
			TransactionReport r = m_holder.getReport();

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
		public void visitType(TransactionType type) {
			TransactionType t = m_holder.getType();

			m_helper.mergeType(t, type);

			Collection<TransactionName> names = new ArrayList<TransactionName>(type.getNames().values());

			for (TransactionName name : names) {
				TransactionName n = t.findOrCreateName(name.getId());

				m_holder.setName(n);
				visitName(name);
			}
		}
	}

	private class NameTailor extends BaseVisitor {
		private String m_typeName;

		private String m_ip;

		private Machine m_machine;

		private TransactionType m_type;

		public NameTailor(String type, String ip) {
			m_typeName = type;
			m_ip = ip;
		}

		@Override
		public void visitMachine(Machine machine) {
			TransactionType type = machine.findType(m_typeName);

			machine.getTypes().clear();

			if (type != null) {
				m_type = m_machine.findOrCreateType(type.getId());

				m_helper.mergeType(m_type, type);
				machine.addType(type);
			}

			super.visitMachine(machine);
		}

		@Override
		public void visitName(TransactionName name) {
			name.getRanges().clear();
			name.getDurations().clear();
			name.getAllDurations().clear();

			TransactionName n = m_type.findOrCreateName(name.getId());

			m_helper.mergeName(n, name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			boolean all = m_ip == null || m_ip.equals(Constants.ALL);

			if (all) {
				m_machine = new Machine(Constants.ALL);
			} else {
				m_machine = new Machine(m_ip);

				Machine m = transactionReport.findMachine(m_ip);
				transactionReport.getMachines().clear();
				transactionReport.addMachine(m);
			}

			super.visitTransactionReport(transactionReport);

			transactionReport.getMachines().clear();
			transactionReport.addMachine(m_machine);
		}

		@Override
		public void visitType(TransactionType type) {
			type.getRange2s().clear();
			type.getAllDurations().clear();

			super.visitType(type);
		}
	}
}
