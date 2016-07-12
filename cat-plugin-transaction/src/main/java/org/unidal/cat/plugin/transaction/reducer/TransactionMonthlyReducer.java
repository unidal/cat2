package org.unidal.cat.plugin.transaction.reducer;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.helper.Dates;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

@Named(type = ReportReducer.class, value = TransactionConstants.NAME + ":" + TransactionMonthlyReducer.ID)
public class TransactionMonthlyReducer extends AbstractTransactionReducer implements ReportReducer<TransactionReport> {
	public static final String ID = MONTHLY;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ReportPeriod getPeriod() {
		return ReportPeriod.MONTH;
	}

	@Override
	protected int getRangeValue(TransactionReport report, Range range) {
		int day = Dates.from(report.getStartTime()).day();

		return day;
	}
}
