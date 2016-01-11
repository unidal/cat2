package org.unidal.cat.plugin.transaction.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.remote.RemoteContext;
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
		TypeScreener screener = new TypeScreener(ip);

		report.accept(screener);
		return screener.getReport();
	}

	@Override
	public void tailor(RemoteContext ctx, TransactionReport report) {
		String ip = ctx.getProperty("ip", null);
		TypeTailor tailor = new TypeTailor(ip);

		report.accept(tailor);
	}

	private class TypeScreener extends BaseVisitor {
		private String m_ip;

		private Machine m_machine;

		private TransactionReport m_report;

		private TransactionType m_type;

		public TypeScreener(String ip) {
			m_ip = ip;
			m_report = new TransactionReport();
		}

		public TransactionReport getReport() {
			return m_report;
		}

		@Override
		public void visitTransactionReport(TransactionReport report) {
			m_report.setDomain(report.getDomain());
			m_helper.mergeReport(m_report, report);

			if (m_ip == null || m_ip.equals(Constants.ALL)) {
				Collection<Machine> machines = new ArrayList<Machine>(report.getMachines().values());

				m_machine = m_report.findOrCreateMachine(Constants.ALL);

				for (Machine machine : machines) {
					visitMachine(machine);
				}
			} else {
				Machine m = report.findMachine(m_ip);

				m_machine = m_report.findOrCreateMachine(m_ip);

				if (m != null) {
					visitMachine(m);
				}
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			m_helper.mergeMachine(m_machine, machine);

			Collection<TransactionType> types = new ArrayList<TransactionType>(machine.getTypes().values());

			for (TransactionType type : types) {
				m_type = m_machine.findOrCreateType(type.getId());

				visitType(type);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			m_helper.mergeType(m_type, type);
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
				transactionReport.addMachine(m);
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
