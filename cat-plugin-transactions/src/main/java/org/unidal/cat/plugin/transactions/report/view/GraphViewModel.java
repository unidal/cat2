package org.unidal.cat.plugin.transactions.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.report.view.LineChart;
import org.unidal.cat.core.report.view.PieChart;
import org.unidal.cat.core.report.view.svg.GraphBuilder;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.BaseVisitor;
import org.unidal.cat.plugin.transactions.report.view.GraphPayload.AverageTimePayload;
import org.unidal.cat.plugin.transactions.report.view.GraphPayload.DurationPayload;
import org.unidal.cat.plugin.transactions.report.view.GraphPayload.FailurePayload;
import org.unidal.cat.plugin.transactions.report.view.GraphPayload.HitPayload;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.helper.Dates;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;

public class GraphViewModel {
   private Map<String, String> m_barCharts = new HashMap<String, String>();

   private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

   private PieChart m_pieChart;

   private List<DistributionDetail> m_distributions;

   public GraphViewModel(GraphBuilder builder, String bu, String type, String name, TransactionsReport report) {
      buildBarGraphs(builder, report, bu, type, name);

      if (bu == null) {
         buildPieChart(report, type, name);
         buildDistribution(report, type, name);
      }
   }

   public GraphViewModel(String bu, String type, String name, TransactionsReport current, TransactionsReport last,
         TransactionsReport baseline) {
      buildLineCharts(bu, type, name, current, last, baseline);

      if (bu == null) {
         buildPieChart(current, type, name);
         buildDistribution(current, type, name);
      }
   }

   private double[] aggregateMetrics(TransactionsReport report, String ip, String type, String name, String metric,
         int size) {
      MetricsAggregator aggregator = new MetricsAggregator(ip, type, name, metric, size);

      report.accept(aggregator);
      return aggregator.getMetrics();
   }

   private void buildBarGraphs(GraphBuilder builder, TransactionsReport report, String bu, String type, String name) {
      TransactionsType t = report.findOrCreateDepartment(bu == null ? Constants.ALL : bu).findOrCreateType(type);
      TransactionsName n = t.findOrCreateName(name == null ? Constants.ALL : name);

      DurationPayload duration = new DurationPayload("Duration Distribution", "Duration (ms)", "Count", n);
      HitPayload hits = new HitPayload("Hits Over Time", "Time (min)", "Count", n);
      AverageTimePayload average = new AverageTimePayload("Average Duration Over Time", "Time (min)",
            "Average Duration (ms)", n);
      FailurePayload failure = new FailurePayload("Failures Over Time", "Time (min)", "Count", n);

      m_barCharts.put("duration", builder.build(duration));
      m_barCharts.put("hits", builder.build(hits));
      m_barCharts.put("average", builder.build(average));
      m_barCharts.put("failures", builder.build(failure));
   }

   private void buildDistribution(TransactionsReport report, String type, String name) {
      DistributionBuilder builder = new DistributionBuilder(type, name);

      report.accept(builder);
      m_distributions = builder.getDetails();
   }

   private LineChart buildLineChart(String ip, String type, String name, TransactionsReport current,
         TransactionsReport last, TransactionsReport baseline, String metric) {
      ReportPeriod period = current.getPeriod();
      Date startTime = current.getStartTime();
      String format;
      String suffix;
      int size;
      long step;

      switch (period) {
      case DAY:
         size = 24;
         step = TimeUnit.HOURS.toMillis(1);
         suffix = " (times/hour)";
         format = "yyyy-MM-dd";
         break;
      case WEEK:
         size = 7;
         step = TimeUnit.DAYS.toMillis(1);
         suffix = " (times/day)";
         format = "yyyy-MM-dd";
         break;
      case MONTH:
         size = 31;
         step = TimeUnit.DAYS.toMillis(1);
         suffix = " (times/day)";
         format = "yyyy-MM";
         break;
      case YEAR:
         size = 12;
         step = TimeUnit.DAYS.toMillis(1);
         suffix = " (times/month)";
         format = "yyyy";
         break;
      default:
         throw new UnsupportedOperationException("Unsupported period: " + period + "!");
      }

      LineChart c = new LineChart();

      c.setStart(startTime);
      c.setSize(size);
      c.setStep(step);

      if ("total".equals(metric)) {
         c.setTitle("Total Distribution" + suffix);
      } else if ("avg".equals(metric)) {
         c.setTitle("Average Distribution (ms)");
      } else if ("failure".equals(metric)) {
         c.setTitle("Failure Distribution" + suffix);
      } else {
         throw new UnsupportedOperationException("Unsupported metric: " + metric + "!");
      }

      if (current != null) {
         c.addSubTitle(Dates.from(current.getStartTime()).asString(format));
         c.addValue(aggregateMetrics(current, ip, type, name, metric, size));
      }

      if (last != null) {
         c.addSubTitle(Dates.from(last.getStartTime()).asString(format));
         c.addValue(aggregateMetrics(last, ip, type, name, metric, size));
      }

      if (baseline != null) {
         c.addSubTitle(Dates.from(baseline.getStartTime()).asString(format));
         c.addValue(aggregateMetrics(baseline, ip, type, name, metric, size));
      }

      return c;
   }

   private void buildLineCharts(String ip, String type, String name, TransactionsReport current,
         TransactionsReport last, TransactionsReport baseline) {
      m_lineCharts.put("hits", buildLineChart(ip, type, name, current, last, baseline, "total"));
      m_lineCharts.put("average", buildLineChart(ip, type, name, current, last, baseline, "avg"));
      m_lineCharts.put("failures", buildLineChart(ip, type, name, current, last, baseline, "failure"));
   }

   private void buildPieChart(TransactionsReport report, String type, String name) {
      PieChartBuilder builder = new PieChartBuilder(type, name);

      report.accept(builder);
      m_pieChart = builder.getPieChart();
   }

   public Map<String, String> getBarCharts() {
      return m_barCharts;
   }

   public List<DistributionDetail> getDistributions() {
      return m_distributions;
   }

   public Map<String, LineChart> getLineCharts() {
      return m_lineCharts;
   }

   public PieChart getPieChart() {
      return m_pieChart;
   }

   public void setPieChart(PieChart pieChart) {
      m_pieChart = pieChart;
   }

   static class DistributionBuilder extends BaseVisitor {
      private String m_type;

      private String m_name;

      private String m_bu;

      private List<DistributionDetail> m_details = new ArrayList<DistributionDetail>();

      public DistributionBuilder(String type, String name) {
         m_type = type;
         m_name = name;
      }

      public List<DistributionDetail> getDetails() {
         Collections.sort(m_details, new Comparator<DistributionDetail>() {
            @Override
            public int compare(DistributionDetail o1, DistributionDetail o2) {
               long gap = o2.getTotalCount() - o1.getTotalCount();

               if (gap > 0) {
                  return 1;
               } else if (gap < 0) {
                  return -1;
               } else {
                  return 0;
               }
            }
         });

         return m_details;
      }

      @Override
      public void visitDepartment(TransactionsDepartment department) {
         String bu = department.getId();

         if (bu != null && !bu.equals(Constants.ALL)) {
            m_bu = bu;

            super.visitDepartment(department);
         }
      }

      @Override
      public void visitName(TransactionsName name) {
         if (m_name.equals(name.getId())) {
            DistributionDetail detail = new DistributionDetail();

            detail.setTotalCount(name.getTotalCount()).setFailCount(name.getFailCount())
                  .setFailPercent(name.getFailPercent()).setIp(m_bu).setAvg(name.getAvg()).setMin(name.getMin())
                  .setMax(name.getMax()).setStd(name.getStd());
            m_details.add(detail);
         }
      }

      @Override
      public void visitType(TransactionsType type) {
         if (m_type != null && m_type.equals(type.getId())) {
            if (StringUtils.isEmpty(m_name)) {
               DistributionDetail detail = new DistributionDetail();

               detail.setTotalCount(type.getTotalCount()).setFailCount(type.getFailCount())
                     .setFailPercent(type.getFailPercent()).setIp(m_bu).setAvg(type.getAvg()).setMin(type.getMin())
                     .setMax(type.getMax()).setStd(type.getStd());
               m_details.add(detail);
            } else {
               super.visitType(type);
            }
         }
      }
   }

   public static class DistributionDetail {
      private String m_bu;

      private long m_totalCount;

      private long m_failCount;

      private double m_failPercent;

      private double m_min;

      private double m_max;

      private double m_avg;

      private double m_std;

      private double m_qps;

      public double getAvg() {
         return m_avg;
      }

      public String getBu() {
         return m_bu;
      }

      public long getFailCount() {
         return m_failCount;
      }

      public double getFailPercent() {
         return m_failPercent;
      }

      public double getMax() {
         return m_max;
      }

      public double getMin() {
         return m_min;
      }

      public double getQps() {
         return m_qps;
      }

      public double getStd() {
         return m_std;
      }

      public long getTotalCount() {
         return m_totalCount;
      }

      public DistributionDetail setAvg(double avg) {
         m_avg = avg;
         return this;
      }

      public DistributionDetail setFailCount(long failCount) {
         m_failCount = failCount;
         return this;
      }

      public DistributionDetail setFailPercent(double failPercent) {
         m_failPercent = failPercent;
         return this;
      }

      public DistributionDetail setIp(String ip) {
         m_bu = ip;
         return this;
      }

      public DistributionDetail setMax(double max) {
         m_max = max;
         return this;
      }

      public DistributionDetail setMin(double min) {
         m_min = min;
         return this;
      }

      public DistributionDetail setQps(double qps) {
         m_qps = qps;
         return this;
      }

      public DistributionDetail setStd(double std) {
         m_std = std;
         return this;
      }

      public DistributionDetail setTotalCount(long totalCount) {
         m_totalCount = totalCount;
         return this;
      }

   }

   private static class MetricsAggregator extends BaseVisitor {
      private double[] m_values;

      private String m_bu;

      private String m_type;

      private String m_name;

      private String m_metric;

      public MetricsAggregator(String ip, String type, String name, String metric, int size) {
         m_values = new double[size];
         m_bu = ip;
         m_type = type;
         m_name = name;
         m_metric = metric;
      }

      public double[] getMetrics() {
         return m_values;
      }

      @Override
      public void visitDepartment(TransactionsDepartment department) {
         if (m_bu == null || m_bu.equals(department.getId())) {
            super.visitDepartment(department);
         }
      }

      @Override
      public void visitName(TransactionsName name) {
         if (m_name == null || m_name.equals(name.getId())) {
            super.visitName(name);
         }
      }

      @Override
      public void visitRange(TransactionsRange range) {
         int index = range.getValue();
         double value;

         if ("total".equals(m_metric)) {
            value = range.getSum();
         } else if ("avg".equals(m_metric)) {
            value = range.getAvg();
         } else if ("failure".equals(m_metric)) {
            value = range.getFails();
         } else {
            throw new UnsupportedOperationException("Unsupported metric: " + m_metric + "!");
         }

         m_values[index] = value;
      }

      @Override
      public void visitType(TransactionsType type) {
         if (m_type == null || m_type.equals(type.getId())) {
            super.visitType(type);
         }
      }
   }

   static class PieChartBuilder extends BaseVisitor {
      private String m_type;

      private String m_name;

      private Map<String, Long> m_items = new HashMap<String, Long>();

      private String m_bu;

      public PieChartBuilder(String type, String name) {
         m_type = type;
         m_name = name;
      }

      public PieChart getPieChart() {
         PieChart chart = new PieChart();

         for (Entry<String, Long> entry : m_items.entrySet()) {
            chart.addItem(entry.getKey(), entry.getValue());
         }

         chart.prepare();
         return chart;
      }

      @Override
      public void visitDepartment(TransactionsDepartment department) {
         String bu = department.getId();

         if (bu != null && !bu.equals(Constants.ALL)) {
            m_bu = bu;

            for (TransactionsType type : department.getTypes().values()) {
               if (m_type != null && m_type.equals(type.getId())) {
                  if (StringUtils.isEmpty(m_name)) {
                     m_items.put(m_bu, type.getTotalCount());
                  } else {
                     for (TransactionsName name : type.getNames().values()) {
                        if (m_name.equals(name.getId())) {
                           m_items.put(m_bu, name.getTotalCount());
                           break;
                        }
                     }
                  }
                  break;
               }
            }
         }
      }
   }
}
