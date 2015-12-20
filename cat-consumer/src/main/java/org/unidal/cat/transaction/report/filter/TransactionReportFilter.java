package org.unidal.cat.transaction.report.filter;

import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.cat.transaction.report.TransactionConstants;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.IVisitor;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

@Named(type = ReportFilter.class, value = TransactionConstants.ID + ":report")
public class TransactionReportFilter implements ReportFilter<TransactionReport> {
	@Override
	public void applyTo(TransactionReport report) {
	}

	@Override
	public void applyTo(RemoteContext ctx, TransactionReport report) {
		String type = ctx.getProperty("type", null);
		String name = ctx.getProperty("name", null);
		String ip = ctx.getProperty("ip", null);
		int min = ctx.getIntProperty("min", -1);
		int max = ctx.getIntProperty("max", -1);

		IVisitor visitor = new Filter(type, name, ip, min, max);
		report.accept(visitor);
	}

	@Override
	public String getId() {
		return "report";
	}

	@Override
	public String getReportName() {
		return TransactionConstants.ID;
	}

	static class Filter extends BaseVisitor {
		private String m_ip;

		private String m_name;

		private String m_type;

		private int m_min;

		private int m_max;

		public Filter(String type, String name, String ip, int min, int max) {
			m_type = type;
			m_name = name;
			m_ip = ip;
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitAllDuration(AllDuration duration) {
		}

		@Override
		public void visitDuration(Duration duration) {
			if (m_type != null && m_name != null) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.transaction.model.entity.Machine machine) {
			if (m_ip == null || m_ip.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ip)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_type != null) {
				super.visitName(name);
			}
		}

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				int minute = range.getValue();

				if (m_min == -1 && m_max == -1) {
					super.visitRange(range);
				} else if (minute <= m_max && minute >= m_min) {
					super.visitRange(range);
				}
			}
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			synchronized (transactionReport) {
				super.visitTransactionReport(transactionReport);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				super.visitType(type);
			}
		}
	}
}
