package org.unidal.cat.plugin.transactions.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.lookup.annotation.Named;

@Named(type = TransactionsHelper.class)
public class TransactionsHelper {
   public void mergeDepartment(TransactionsDepartment dst, TransactionsDepartment src) {
      // Do nothing
   }

   public void mergeDurations(Map<Integer, TransactionsDuration> dst, Map<Integer, TransactionsDuration> src) {
      for (Map.Entry<Integer, TransactionsDuration> e : src.entrySet()) {
         Integer key = e.getKey();
         TransactionsDuration duration = e.getValue();
         TransactionsDuration oldDuration = dst.get(key);

         if (oldDuration == null) {
            oldDuration = new TransactionsDuration(duration.getValue());
            dst.put(key, oldDuration);
         }

         oldDuration.setCount(oldDuration.getCount() + duration.getCount());
      }
   }

   public void mergeName(TransactionsName dst, TransactionsName src) {
      long totalCountSum = dst.getTotalCount() + src.getTotalCount();
      if (totalCountSum > 0) {
         double line95Values = dst.getLine95Value() * dst.getTotalCount() + src.getLine95Value() * src.getTotalCount();
         double line99Values = dst.getLine99Value() * dst.getTotalCount() + src.getLine99Value() * src.getTotalCount();

         dst.setLine95Value(line95Values / totalCountSum);
         dst.setLine99Value(line99Values / totalCountSum);
      }

      dst.setTotalCount(totalCountSum);
      dst.setFailCount(dst.getFailCount() + src.getFailCount());
      dst.setTps(dst.getTps() + src.getTps());

      if (src.getMin() < dst.getMin()) {
         dst.setMin(src.getMin());
      }

      if (src.getMax() > dst.getMax()) {
         dst.setMax(src.getMax());
         dst.setSlowestMessageUrl(src.getSlowestMessageUrl());
      }

      dst.setSum(dst.getSum() + src.getSum());
      dst.setSum2(dst.getSum2() + src.getSum2());

      if (dst.getTotalCount() > 0) {
         dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
         dst.setAvg(dst.getSum() / dst.getTotalCount());
         dst.setStd(std(dst.getTotalCount(), dst.getAvg(), dst.getSum2(), dst.getMax()));
      }

      if (dst.getSuccessMessageUrl() == null) {
         dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
      }

      if (dst.getFailMessageUrl() == null) {
         dst.setFailMessageUrl(src.getFailMessageUrl());
      }
   }

   public void mergeRanges(List<TransactionsRange> dst, List<TransactionsRange> src) {
      Map<Integer, Integer> map = new HashMap<Integer, Integer>();

      for (int i = dst.size() - 1; i >= 0; i--) {
         TransactionsRange duration = dst.get(i);

         map.put(duration.getValue(), i);
      }

      for (int i = 0; i < src.size(); i++) {
         TransactionsRange duration = src.get(i);
         Integer index = map.get(duration.getValue());
         TransactionsRange oldRange;

         if (index == null) {
            oldRange = new TransactionsRange(duration.getValue());
            dst.add(oldRange);
         } else {
            oldRange = dst.get(index);
         }

         oldRange.setCount(oldRange.getCount() + duration.getCount());
         oldRange.setFails(oldRange.getFails() + duration.getFails());
         oldRange.setSum(oldRange.getSum() + duration.getSum());

         if (oldRange.getCount() > 0) {
            oldRange.setAvg(oldRange.getSum() / oldRange.getCount());
         } else {
            oldRange.setAvg(0);
         }
      }
   }

   public void mergeReport(TransactionsReport dst, TransactionsReport src) {
      dst.mergeAttributes(src);
      dst.getBus().addAll(src.getBus());
   }

   public void mergeType(TransactionsType dst, TransactionsType src) {
      long totalCountSum = dst.getTotalCount() + src.getTotalCount();
      if (totalCountSum > 0) {
         double line95Values = dst.getLine95Value() * dst.getTotalCount() + src.getLine95Value() * src.getTotalCount();
         double line99Values = dst.getLine99Value() * dst.getTotalCount() + src.getLine99Value() * src.getTotalCount();

         dst.setLine95Value(line95Values / totalCountSum);
         dst.setLine99Value(line99Values / totalCountSum);
      }

      dst.setTotalCount(totalCountSum);
      dst.setFailCount(dst.getFailCount() + src.getFailCount());
      dst.setTps(dst.getTps() + src.getTps());

      if (src.getMin() < dst.getMin()) {
         dst.setMin(src.getMin());
      }

      if (src.getMax() > dst.getMax()) {
         dst.setMax(src.getMax());
         dst.setSlowestMessageUrl(src.getSlowestMessageUrl());
      }

      dst.setSum(dst.getSum() + src.getSum());
      dst.setSum2(dst.getSum2() + src.getSum2());

      if (dst.getTotalCount() > 0) {
         dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
         dst.setAvg(dst.getSum() / dst.getTotalCount());
         dst.setStd(std(dst.getTotalCount(), dst.getAvg(), dst.getSum2(), dst.getMax()));
      }

      if (dst.getSuccessMessageUrl() == null) {
         dst.setSuccessMessageUrl(src.getSuccessMessageUrl());
      }

      if (dst.getFailMessageUrl() == null) {
         dst.setFailMessageUrl(src.getFailMessageUrl());
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
}
