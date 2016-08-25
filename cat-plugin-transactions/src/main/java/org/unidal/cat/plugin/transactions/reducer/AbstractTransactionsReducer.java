package org.unidal.cat.plugin.transactions.reducer;

import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transactions.filter.TransactionsHelper;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.lookup.annotation.Inject;

public abstract class AbstractTransactionsReducer implements ReportReducer<TransactionsReport> {
   @Inject
   private TransactionsHelper m_helper;

   protected abstract int getRangeValue(TransactionsReport report, TransactionsRange range);

   @Override
   public String getReportName() {
      return TransactionConstants.NAME;
   }

   @Override
   public TransactionsReport reduce(List<TransactionsReport> reports) {
      TransactionsReport r = new TransactionsReport();

      if (!reports.isEmpty()) {
         TransactionsReport first = reports.get(0);
         Merger merger = new Merger(r);

         for (TransactionsReport report : reports) {
            report.accept(merger.setMapping(new ValueMapping(report)));
         }

         ReportPeriod period = getPeriod();

         r.setPeriod(period);
         r.setStartTime(period.getStartTime(first.getStartTime()));
      }

      return r;
   }

   protected class Merger extends DefaultMerger {
      private TransactionsRangeMapping m_mapping;

      public Merger(TransactionsReport report) {
         super(report);
      }

      @Override
      protected void mergeDuration(TransactionsDuration old, TransactionsDuration other) {
         m_helper.mergeDuration(old, other);
      }

      @Override
      protected void mergeName(TransactionsName old, TransactionsName other) {
         m_helper.mergeName(old, other);
      }

      @Override
      protected void mergeRange(TransactionsRange old, TransactionsRange other) {
         m_helper.mergeRange(old, other);
      }

      @Override
      protected void mergeType(TransactionsType old, TransactionsType other) {
         m_helper.mergeType(old, other);
      }

      public Merger setMapping(TransactionsRangeMapping mapping) {
         m_mapping = mapping;
         return this;
      }

      protected void visitNameChildren(TransactionsName to, TransactionsName from) {
         for (TransactionsRange source : from.getRanges()) {
            int value = m_mapping.getValue(source);
            TransactionsRange r = to.findOrCreateRange(value);

            getObjects().push(r);
            source.accept(this);
            getObjects().pop();
         }

         for (TransactionsDuration source : from.getDurations().values()) {
            TransactionsDuration target = to.findDuration(source.getValue());

            if (target == null) {
               target = new TransactionsDuration(source.getValue());
               to.addDuration(target);
            }

            getObjects().push(target);
            source.accept(this);
            getObjects().pop();
         }
      }
   }

   protected static interface TransactionsRangeMapping {
      public int getValue(TransactionsRange range);
   }

   class ValueMapping implements TransactionsRangeMapping {
      private TransactionsReport m_report;

      public ValueMapping(TransactionsReport report) {
         m_report = report;
      }

      @Override
      public int getValue(TransactionsRange range) {
         return getRangeValue(m_report, range);
      }
   }
}
