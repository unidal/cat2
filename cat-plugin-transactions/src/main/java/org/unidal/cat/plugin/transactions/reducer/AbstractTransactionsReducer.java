package org.unidal.cat.plugin.transactions.reducer;

import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;

public abstract class AbstractTransactionsReducer implements ReportReducer<TransactionsReport> {
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

   protected static class Merger extends DefaultMerger {
      private TransactionsRangeMapping m_mapping;

      public Merger(TransactionsReport report) {
         super(report);
      }

      @Override
      protected void mergeDuration(TransactionsDuration old, TransactionsDuration duration) {
         old.setCount(old.getCount() + duration.getCount());
         old.setValue(duration.getValue());
      }

      @Override
      protected void mergeName(TransactionsName old, TransactionsName other) {
         long totalCount = old.getTotalCount() + other.getTotalCount();
         if (totalCount > 0) {
            double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
                  * other.getTotalCount();
            double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
                  * other.getTotalCount();

            old.setLine95Value(line95Values / totalCount);
            old.setLine99Value(line99Values / totalCount);
         }

         old.setTotalCount(totalCount);
         old.setFailCount(old.getFailCount() + other.getFailCount());
         old.setTps(old.getTps() + other.getTps());

         if (other.getMin() < old.getMin()) {
            old.setMin(other.getMin());
         }

         if (other.getMax() > old.getMax()) {
            old.setMax(other.getMax());
            old.setSlowestMessageUrl(other.getSlowestMessageUrl());
         }

         old.setSum(old.getSum() + other.getSum());
         old.setSum2(old.getSum2() + other.getSum2());

         if (old.getTotalCount() > 0) {
            old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
            old.setAvg(old.getSum() / old.getTotalCount());
            old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
         }

         if (old.getSuccessMessageUrl() == null) {
            old.setSuccessMessageUrl(other.getSuccessMessageUrl());
         }

         if (old.getFailMessageUrl() == null) {
            old.setFailMessageUrl(other.getFailMessageUrl());
         }
      }

      @Override
      protected void mergeRange(TransactionsRange old, TransactionsRange range) {
         old.setCount(old.getCount() + range.getCount());
         old.setFails(old.getFails() + range.getFails());
         old.setSum(old.getSum() + range.getSum());

         if (old.getCount() > 0) {
            old.setAvg(old.getSum() / old.getCount());
         }
      }

      @Override
      protected void mergeType(TransactionsType old, TransactionsType other) {
         long totalCount = old.getTotalCount() + other.getTotalCount();

         if (totalCount > 0) {
            double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
                  * other.getTotalCount();
            double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
                  * other.getTotalCount();

            old.setLine95Value(line95Values / totalCount);
            old.setLine99Value(line99Values / totalCount);
         }

         old.setTotalCount(totalCount);
         old.setFailCount(old.getFailCount() + other.getFailCount());
         old.setTps(old.getTps() + other.getTps());

         if (other.getMin() < old.getMin()) {
            old.setMin(other.getMin());
         }

         if (other.getMax() > old.getMax()) {
            old.setMax(other.getMax());
            old.setSlowestMessageUrl(other.getSlowestMessageUrl());
         }

         old.setSum(old.getSum() + other.getSum());
         old.setSum2(old.getSum2() + other.getSum2());

         if (old.getTotalCount() > 0) {
            old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
            old.setAvg(old.getSum() / old.getTotalCount());
            old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
         }

         if (old.getSuccessMessageUrl() == null) {
            old.setSuccessMessageUrl(other.getSuccessMessageUrl());
         }

         if (old.getFailMessageUrl() == null) {
            old.setFailMessageUrl(other.getFailMessageUrl());
         }
      }

      public Merger setMapping(TransactionsRangeMapping mapping) {
         m_mapping = mapping;
         return this;
      }

      double std(long count, double avg, double sum2, double max) {
         double value = sum2 / count - avg * avg;

         if (value <= 0 || count <= 1) {
            return 0;
         } else if (count == 2) {
            return max - avg;
         } else {
            return Math.sqrt(value);
         }
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
