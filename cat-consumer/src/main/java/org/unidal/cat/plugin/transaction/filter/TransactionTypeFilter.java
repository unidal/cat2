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
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

@Named(type = ReportFilter.class, value = TransactionConstants.NAME + ":" + TransactionTypeFilter.ID)
public class TransactionTypeFilter implements ReportFilter<TransactionReport> {
	public static final String ID = "type";

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
		String ip = ctx.getProperty("ip", null);
		TypeScreener visitor = new TypeScreener(report.getDomain(), ip);

		report.accept(visitor);
		return visitor.getReport();
	}

	@Override
	public void tailor(RemoteContext ctx, TransactionReport report) {
		String ip = ctx.getProperty("ip", null);
		TypeTailor visitor = new TypeTailor(ip);

		report.accept(visitor);
	}

	private class TypeScreener extends BaseVisitor {
		private String m_ip;

		private TransactionHolder m_holder = new TransactionHolder();

		public TypeScreener(String domain, String ip) {
			m_ip = ip;
			m_holder.setReport(new TransactionReport(domain));
		}

		public TransactionReport getReport() {
			return m_holder.getReport();
		}

		@Override
		public void visitMachine(Machine machine) {
			Machine m = m_holder.getMachine();

			m_helper.mergeMachine(m, machine);

			Collection<TransactionType> types = new ArrayList<TransactionType>(machine.getTypes().values());

			for (TransactionType type : types) {
				TransactionType t = m.findOrCreateType(type.getId());

				m_holder.setType(t);
				visitType(type);
			}
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
		}
	}

	private class TypeTailor extends BaseVisitor {
		private String m_ip;

		private Machine m_machine;

		public TypeTailor(String ip) {
			m_ip = ip;
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

				if (m != null) {
					transactionReport.addMachine(m);
				}
			}

			super.visitTransactionReport(transactionReport);

			transactionReport.getMachines().clear();
			transactionReport.addMachine(m_machine);
		}

		@Override
		public void visitType(TransactionType type) {
			type.getNames().clear();
			type.getRange2s().clear();
			type.getAllDurations().clear();

			TransactionType t = m_machine.findOrCreateType(type.getId());

			m_helper.mergeType(t, type);
		}
	}
}
