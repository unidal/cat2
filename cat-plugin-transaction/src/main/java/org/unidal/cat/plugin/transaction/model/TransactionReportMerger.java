package org.unidal.cat.plugin.transaction.model;

import org.unidal.cat.plugin.transaction.filter.TransactionHelper;
import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.DefaultMerger;

public class TransactionReportMerger extends DefaultMerger {
   private TransactionHelper m_helper;

   public TransactionReportMerger(TransactionHelper helper, TransactionReport transactionReport) {
      super(transactionReport);

      m_helper = helper;
   }

   @Override
   public void mergeDuration(Duration old, Duration other) {
      m_helper.mergeDuration(old, other);
   }

   @Override
   public void mergeMachine(Machine old, Machine other) {
      m_helper.mergeMachine(old, other);
   }

   @Override
   public void mergeName(TransactionName old, TransactionName other) {
      m_helper.mergeName(old, other);
   }

   @Override
   public void mergeRange(Range old, Range other) {
      m_helper.mergeRange(old, other);
   }

   @Override
   public void mergeType(TransactionType old, TransactionType other) {
      m_helper.mergeType(old, other);
   }

   @Override
   public void visitTransactionReport(TransactionReport report) {
      super.visitTransactionReport(report);

      getTransactionReport().getDomainNames().addAll(report.getDomainNames());
      getTransactionReport().getIps().addAll(report.getIps());
   }
}
