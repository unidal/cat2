package org.unidal.cat.plugin.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = TransactionConstants.NAME)
public class TransactionReportManager extends AbstractReportManager<TransactionReport> {
   @Override
   public int getThreadsCount() {
      return 2;
   }
}
