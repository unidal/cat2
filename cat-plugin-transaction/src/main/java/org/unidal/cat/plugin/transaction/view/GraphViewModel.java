package org.unidal.cat.plugin.transaction.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.plugin.transaction.view.GraphPayload.AverageTimePayload;
import org.unidal.cat.plugin.transaction.view.GraphPayload.DurationPayload;
import org.unidal.cat.plugin.transaction.view.GraphPayload.FailurePayload;
import org.unidal.cat.plugin.transaction.view.GraphPayload.HitPayload;
import org.unidal.cat.plugin.transaction.view.GraphViewModel.DistributionBuilder.DistributionDetail;
import org.unidal.cat.plugin.transaction.view.svg.GraphBuilder;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;

public class GraphViewModel {
	private Map<String, String> m_svgCharts = new HashMap<String, String>();

	private PieChart m_pieChart;

	private List<DistributionDetail> m_distributions;

	public GraphViewModel(GraphBuilder builder, TransactionReport report, String ip, String type, String name) {
		buildSvgGraphs(builder, report, ip, type, name);

		if (Constants.ALL.equalsIgnoreCase(ip)) {
			buildPieChart(report, type, name);
			buildDistribution(report, type, name);
		}
	}

	public List<DistributionDetail> getDistributions() {
		return m_distributions;
	}

	private void buildDistribution(TransactionReport report, String type, String name) {
		DistributionBuilder builder = new DistributionBuilder(type, name);

		report.accept(builder);
		m_distributions = builder.getDetails();
	}

	private void buildPieChart(TransactionReport report, String type, String name) {
		PieChartBuilder builder = new PieChartBuilder(type, name);

		report.accept(builder);
		m_pieChart = builder.getPieChart();
	}

	private void buildSvgGraphs(GraphBuilder builder, TransactionReport report, String ip, String type, String name) {
		if (name == null || name.length() == 0) {
			name = Constants.ALL;
		}

		TransactionType t = report.findOrCreateMachine(ip).findOrCreateType(type);
		TransactionName n = t.findOrCreateName(name);

		if (n != null) {
			DurationPayload duration = new DurationPayload("Duration Distribution", "Duration (ms)", "Count", n);
			HitPayload hits = new HitPayload("Hits Over Time", "Time (min)", "Count", n);
			AverageTimePayload average = new AverageTimePayload("Average Duration Over Time", "Time (min)",
			      "Average Duration (ms)", n);
			FailurePayload failure = new FailurePayload("Failures Over Time", "Time (min)", "Count", n);

			m_svgCharts.put("duration", builder.build(duration));
			m_svgCharts.put("hits", builder.build(hits));
			m_svgCharts.put("average", builder.build(average));
			m_svgCharts.put("failures", builder.build(failure));
		}
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public Map<String, String> getSvgCharts() {
		return m_svgCharts;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	static class PieChartBuilder extends BaseVisitor {
		private String m_type;

		private String m_name;

		private Map<String, Long> m_items = new HashMap<String, Long>();

		private String m_ip;

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
		public void visitMachine(Machine machine) {
			if (!Constants.ALL.equalsIgnoreCase(machine.getIp())) {
				m_ip = machine.getIp();

				for (TransactionType type : machine.getTypes().values()) {
					if (m_type != null && m_type.equals(type.getId())) {
						if (StringUtils.isEmpty(m_name)) {
							m_items.put(m_ip, type.getTotalCount());
						} else {
							for (TransactionName name : type.getNames().values()) {
								if (m_name.equals(name.getId())) {
									m_items.put(m_ip, name.getTotalCount());
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

	static class DistributionBuilder extends BaseVisitor {
		private String m_type;

		private String m_name;

		private String m_ip;

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
		public void visitMachine(Machine machine) {
			if (!Constants.ALL.equals(machine.getIp())) {
				m_ip = machine.getIp();

				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_name.equals(name.getId())) {
				DistributionDetail detail = new DistributionDetail();

				detail.setTotalCount(name.getTotalCount()).setFailCount(name.getFailCount())
				      .setFailPercent(name.getFailPercent()).setIp(m_ip).setAvg(name.getAvg()).setMin(name.getMin())
				      .setMax(name.getMax()).setStd(name.getStd());
				m_details.add(detail);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type != null && m_type.equals(type.getId())) {
				if (StringUtils.isEmpty(m_name)) {
					DistributionDetail detail = new DistributionDetail();

					detail.setTotalCount(type.getTotalCount()).setFailCount(type.getFailCount())
					      .setFailPercent(type.getFailPercent()).setIp(m_ip).setAvg(type.getAvg()).setMin(type.getMin())
					      .setMax(type.getMax()).setStd(type.getStd());
					m_details.add(detail);
				} else {
					super.visitType(type);
				}
			}
		}

		public static class DistributionDetail {

			private String m_ip;

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

			public long getFailCount() {
				return m_failCount;
			}

			public double getFailPercent() {
				return m_failPercent;
			}

			public String getIp() {
				return m_ip;
			}

			public double getMax() {
				return m_max;
			}

			public double getMin() {
				return m_min;
			}

			public double getStd() {
				return m_std;
			}

			public double getQps() {
				return m_qps;
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
				m_ip = ip;
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

			public DistributionDetail setStd(double std) {
				m_std = std;
				return this;
			}

			public DistributionDetail setQps(double qps) {
				m_qps = qps;
				return this;
			}

			public DistributionDetail setTotalCount(long totalCount) {
				m_totalCount = totalCount;
				return this;
			}

		}
	}
}
