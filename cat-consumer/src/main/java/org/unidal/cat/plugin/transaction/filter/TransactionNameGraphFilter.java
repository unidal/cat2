package org.unidal.cat.plugin.transaction.filter;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.IVisitor;
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
	public void tailor(RemoteContext ctx, TransactionReport report) {
		String type = ctx.getProperty("type", null);
		String name = ctx.getProperty("name", null);
		String ip = ctx.getProperty("ip", null);

		IVisitor visitor = new NameGraphTailor(type, name, ip);

		report.accept(visitor);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getReportName() {
		return TransactionConstants.NAME;
	}

	private class NameGraphTailor extends BaseVisitor {
		private String m_typeName;

		private String m_name;

		private String m_ip;

		private Machine m_machine;

		private TransactionType m_type;

		public NameGraphTailor(String type, String name, String ip) {
			m_typeName = type;
			m_name = name;
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

			transactionReport.addMachine(m_machine);
		}

		@Override
		public void visitType(TransactionType type) {
			TransactionName n = m_type.findOrCreateName(m_name);
			TransactionName name = type.findName(m_name);

			if (name != null) {
				m_helper.mergeDurations(n.getDurations(), name.getDurations());
				m_helper.mergeRanges(n.getRanges(), name.getRanges());
			}

			m_type.setSuccessMessageUrl(null);
			m_type.setFailMessageUrl(null);
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

	@Override
   public TransactionReport screen(RemoteContext ctx, TransactionReport report) {
	   return report;
   }
}
