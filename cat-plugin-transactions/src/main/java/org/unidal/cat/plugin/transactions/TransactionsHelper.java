package org.unidal.cat.plugin.transactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.lookup.annotation.Named;

@Named(type = TransactionsHelper.class)
public class TransactionsHelper {
   public void mergeDurations(Map<Integer, Duration> dst, Map<Integer, Duration> src) {
      for (Map.Entry<Integer, Duration> e : src.entrySet()) {
         Integer key = e.getKey();
         Duration duration = e.getValue();
         Duration oldDuration = dst.get(key);

         if (oldDuration == null) {
            oldDuration = new Duration(duration.getValue());
            dst.put(key, oldDuration);
         }

         oldDuration.setCount(oldDuration.getCount() + duration.getCount());
      }
   }

   public void mergeMachine(Machine old, Machine other) {
      // nothing to do
   }

   public void mergeName(TransactionName dst, TransactionName src) {
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

   public void mergeRanges(List<Range> dst, List<Range> src) {
      Map<Integer, Integer> map = new HashMap<Integer, Integer>();

      for (int i = dst.size() - 1; i >= 0; i--) {
         Range duration = dst.get(i);

         map.put(duration.getValue(), i);
      }

      for (int i = 0; i < src.size(); i++) {
         Range duration = src.get(i);
         Integer index = map.get(duration.getValue());
         Range oldRange;

         if (index == null) {
            oldRange = new Range(duration.getValue());
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

   public void mergeReport(TransactionReport dst, TransactionReport src) {
      dst.mergeAttributes(src);
      dst.getDomainNames().addAll(src.getDomainNames());
      dst.getIps().addAll(src.getIps());
   }

   public void mergeType(TransactionType dst, TransactionType src) {
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
