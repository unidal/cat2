package org.unidal.cat.plugin.transaction.reducer;

import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

@Named(type = ReportReducer.class, value = TransactionConstants.NAME + ":" + TransactionDailyReducer.ID)
public class TransactionDailyReducer implements ReportReducer<TransactionReport> {
	public static final String ID = "daily";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ReportPeriod getPeriod() {
		return ReportPeriod.HOUR;
	}

	@Override
	public String getReportName() {
		return TransactionConstants.NAME;
	}

	@Override
	public TransactionReport reduce(Context ctx, List<TransactionReport> reports) {
		// TODO Auto-generated method stub
		return null;
	}

}
