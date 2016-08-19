package org.unidal.cat.plugin.transactions.model;

import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.DefaultMerger;

public class TransactionsReportMerger extends DefaultMerger {
   public TransactionsReportMerger(TransactionsReport transactionsReport) {
      super(transactionsReport);
   }

   @Override
   public void mergeDuration(TransactionsDuration old, TransactionsDuration duration) {
      old.setCount(old.getCount() + duration.getCount());
      old.setValue(duration.getValue());
   }

   @Override
   public void mergeName(TransactionsName old, TransactionsName other) {
      long totalCountSum = old.getTotalCount() + other.getTotalCount();
      if (totalCountSum > 0) {
         double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
               * other.getTotalCount();
         double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
               * other.getTotalCount();

         old.setLine95Value(line95Values / totalCountSum);
         old.setLine99Value(line99Values / totalCountSum);
      }

      old.setTotalCount(totalCountSum);
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
   public void mergeRange(TransactionsRange old, TransactionsRange range) {
      old.setCount(old.getCount() + range.getCount());
      old.setFails(old.getFails() + range.getFails());
      old.setSum(old.getSum() + range.getSum());

      if (old.getCount() > 0) {
         old.setAvg(old.getSum() / old.getCount());
      }
   }

   @Override
   public void mergeType(TransactionsType old, TransactionsType other) {
      long totalCountSum = old.getTotalCount() + other.getTotalCount();
      if (totalCountSum > 0) {
         double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
               * other.getTotalCount();
         double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
               * other.getTotalCount();

         old.setLine95Value(line95Values / totalCountSum);
         old.setLine99Value(line99Values / totalCountSum);
      }

      old.setTotalCount(totalCountSum);
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

   @Override
   public void visitTransactionsReport(TransactionsReport transactionsReport) {
      super.visitTransactionsReport(transactionsReport);
      getTransactionsReport().getBus().addAll(transactionsReport.getBus());
   }
}
