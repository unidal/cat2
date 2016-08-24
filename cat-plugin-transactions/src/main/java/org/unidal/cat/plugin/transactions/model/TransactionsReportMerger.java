package org.unidal.cat.plugin.transactions.model;

import org.unidal.cat.plugin.transactions.filter.TransactionsHelper;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.DefaultMerger;

public class TransactionsReportMerger extends DefaultMerger {
   private TransactionsHelper m_helper;

   public TransactionsReportMerger(TransactionsHelper helper, TransactionsReport transactionsReport) {
      super(transactionsReport);

      m_helper = helper;
   }

   @Override
   public void mergeDuration(TransactionsDuration old, TransactionsDuration other) {
      m_helper.mergeDuration(old, other);
   }

   @Override
   public void mergeName(TransactionsName old, TransactionsName other) {
      m_helper.mergeName(old, other);
   }

   @Override
   public void mergeRange(TransactionsRange old, TransactionsRange other) {
      m_helper.mergeRange(old, other);
   }

   @Override
   public void mergeType(TransactionsType old, TransactionsType other) {
      m_helper.mergeType(old, other);
   }

   @Override
   public void visitTransactionsReport(TransactionsReport report) {
      super.visitTransactionsReport(report);

      getTransactionsReport().getBus().addAll(report.getBus());
   }
}
