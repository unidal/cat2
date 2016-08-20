package org.unidal.cat.plugin.transactions.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.config.DomainOrgConfigService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.filter.TransactionHolder;
import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDomain;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.ReportManagerManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = TransactionsConstants.NAME)
public class TransactionsReportManager extends AbstractReportManager<TransactionsReport> {
   @Inject
   private ReportManagerManager m_rmm;

   @Inject
   private DomainOrgConfigService m_config;

   private TransactionsReport buildReport(int hour, String bu) {
      ReportManager<TransactionReport> rm = m_rmm.getReportManager(TransactionConstants.NAME);
      List<Map<String, TransactionReport>> list = rm.getLocalReports(hour);
      TransactionsReportMaker maker = new TransactionsReportMaker();

      for (Map<String, TransactionReport> map : list) {
         for (Map.Entry<String, TransactionReport> e : map.entrySet()) {
            if (bu == null || m_config.isIn(bu, e.getKey())) {
               e.getValue().accept(maker);
            }
         }
      }

      TransactionsReport report = maker.getReport();
      return report;
   }

   /**
    * prepares transactions report for persistance.
    */
   @Override
   @SuppressWarnings("unchecked")
   public List<Map<String, TransactionsReport>> getLocalReports(int hour) {
      TransactionsReport report = buildReport(hour, null);
      Map<String, TransactionsReport> map = new HashMap<String, TransactionsReport>();

      map.put(com.dianping.cat.Constants.ALL, report);
      return Arrays.asList(map);
   }

   /**
    * builds transactions report from transaction reports dynamically
    */
   @Override
   public List<TransactionsReport> getReports(ReportPeriod period, Date startTime, String domain,
         Map<String, String> properties) throws IOException {
      String bu = properties == null ? null : properties.get("bu");
      int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime.getTime());
      TransactionsReport report = buildReport(hour, bu);

      return Arrays.asList(report);
   }

   @Override
   public int getThreadsCount() {
      return 1;
   }

   class TransactionsReportMaker extends BaseVisitor {
      private TransactionHolder m_t;

      private TransactionsHolder m_ts;

      public TransactionsReportMaker() {
         m_t = new TransactionHolder();
         m_ts = new TransactionsHolder();
         m_ts.setReport(new TransactionsReport());
      }

      public TransactionsReport getReport() {
         return m_ts.getReport();
      }

      private void mergeDomain(TransactionsDomain dst, TransactionName src) {
         long totalCountSum = dst.getTotalCount() + src.getTotalCount();

         dst.setTotalCount(totalCountSum);
         dst.setFailCount(dst.getFailCount() + src.getFailCount());
         dst.setTps(dst.getTps() + src.getTps());

         if (src.getMin() < dst.getMin()) {
            dst.setMin(src.getMin());
         }

         if (src.getMax() > dst.getMax()) {
            dst.setMax(src.getMax());
         }

         dst.setSum(dst.getSum() + src.getSum());
         dst.setSum2(dst.getSum2() + src.getSum2());

         if (dst.getTotalCount() > 0) {
            dst.setFailPercent(dst.getFailCount() * 100.0 / dst.getTotalCount());
            dst.setAvg(dst.getSum() / dst.getTotalCount());
            dst.setStd(std(dst.getTotalCount(), dst.getAvg(), dst.getSum2(), dst.getMax()));
         }
      }

      private void mergeDurations(Map<Integer, TransactionsDuration> dst, Map<Integer, Duration> src) {
         for (Map.Entry<Integer, Duration> e : src.entrySet()) {
            Integer key = e.getKey();
            Duration duration = e.getValue();
            TransactionsDuration oldDuration = dst.get(key);

            if (oldDuration == null) {
               oldDuration = new TransactionsDuration(duration.getValue());
               dst.put(key, oldDuration);
            }

            oldDuration.setCount(oldDuration.getCount() + duration.getCount());
         }
      }

      private void mergeName(TransactionsName dst, TransactionName src) {
         long totalCountSum = dst.getTotalCount() + src.getTotalCount();
         if (totalCountSum > 0) {
            double line95Values = dst.getLine95Value() * dst.getTotalCount() + src.getLine95Value()
                  * src.getTotalCount();
            double line99Values = dst.getLine99Value() * dst.getTotalCount() + src.getLine99Value()
                  * src.getTotalCount();

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

      private void mergeRanges(List<TransactionsRange> dst, List<Range> src) {
         Map<Integer, Integer> map = new HashMap<Integer, Integer>();

         for (int i = dst.size() - 1; i >= 0; i--) {
            TransactionsRange duration = dst.get(i);

            map.put(duration.getValue(), i);
         }

         for (int i = 0; i < src.size(); i++) {
            Range duration = src.get(i);
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

      private void mergeType(TransactionsType dst, TransactionType src) {
         long totalCountSum = dst.getTotalCount() + src.getTotalCount();
         if (totalCountSum > 0) {
            double line95Values = dst.getLine95Value() * dst.getTotalCount() + src.getLine95Value()
                  * src.getTotalCount();
            double line99Values = dst.getLine99Value() * dst.getTotalCount() + src.getLine99Value()
                  * src.getTotalCount();

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

      private double std(long count, double avg, double sum2, double max) {
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
      public void visitDuration(Duration duration) {
      }

      @Override
      public void visitMachine(Machine machine) {
         m_t.setMachine(machine);

         super.visitMachine(machine);
      }

      @Override
      public void visitName(TransactionName name) {
         TransactionsName n = m_ts.getType().findOrCreateName(name.getId());
         TransactionsDomain d = n.findOrCreateDomain(m_t.getReport().getDomain());

         m_ts.setName(n);
         mergeName(n, name);

         m_ts.setDomain(d);
         mergeDomain(d, name);

         mergeRanges(n.getRanges(), name.getRanges());
         mergeDurations(n.getDurations(), name.getDurations());

         super.visitName(name);
      }

      @Override
      public void visitTransactionReport(TransactionReport report) {
         TransactionsReport r = m_ts.getReport();
         String d = m_config.findDepartment(report.getDomain());

         r.setPeriod(report.getPeriod()).addBu(d);
         r.setStartTime(report.getStartTime()).setEndTime(report.getEndTime());
         m_t.setReport(report);
         m_ts.setDepartment(r.findOrCreateDepartment(d));

         super.visitTransactionReport(report);
      }

      @Override
      public void visitType(TransactionType type) {
         TransactionsType t = m_ts.getDepartment().findOrCreateType(type.getId());

         m_ts.setType(t);
         mergeType(t, type);

         super.visitType(type);
      }
   }
}
