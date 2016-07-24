package org.unidal.cat.plugin.transaction.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.page.transform.DistributionDetailVisitor.DistributionDetail;
import org.unidal.cat.plugin.transaction.view.PieChart;
import org.unidal.cat.plugin.transaction.view.TableViewModel;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

@ModelMeta(TransactionAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private List<String> m_groups;

	private List<String> m_groupIps;

	private String m_errorTrend;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	private String m_hitTrend;

	private String m_mobileResponse;

	private String m_queryName;

	@EntityMeta
	private TransactionReport m_report;

	private String m_responseTrend;

	private String m_type;

	private String m_distributionChart;

	private List<DistributionDetail> m_distributionDetails;

	// cat2
	private TableViewModel<?> m_table;

	private PieChart m_pieChart;

	private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.REPORT;
	}

	public String getDistributionChart() {
		return m_distributionChart;
	}

	public List<DistributionDetail> getDistributionDetails() {
		return m_distributionDetails;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	// required by report tag
	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return SortHelper.sortDomain(domainNames);
		}
	}

	public String getErrorTrend() {
		return m_errorTrend;
	}

	public String getGraph1() {
		return m_graph1;
	}

	public String getGraph2() {
		return m_graph2;
	}

	public String getGraph3() {
		return m_graph3;
	}

	public String getGraph4() {
		return m_graph4;
	}

	public List<String> getGroupIps() {
		return m_groupIps;
	}

	public List<String> getGroups() {
		return m_groups;
	}

	public String getHitTrend() {
		return m_hitTrend;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public Map<String, LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public TransactionReport getReport() {
		return m_report;
	}

	public String getResponseTrend() {
		return m_responseTrend;
	}

	public TableViewModel<?> getTable() {
		return m_table;
	}

	public String getType() {
		return m_type;
	}

	public void setDistributionChart(String distributionChart) {
		m_distributionChart = distributionChart;
	}

	public void setDistributionDetails(List<DistributionDetail> distributionDetails) {
		m_distributionDetails = distributionDetails;
	}

	public void setErrorTrend(String errorTrend) {
		m_errorTrend = errorTrend;
	}

	public void setGraph1(String graph1) {
		m_graph1 = graph1;
	}

	public void setGraph2(String graph2) {
		m_graph2 = graph2;
	}

	public void setGraph3(String graph3) {
		m_graph3 = graph3;
	}

	public void setGraph4(String graph4) {
		m_graph4 = graph4;
	}

	public void setGroupIps(List<String> groupIps) {
		m_groupIps = groupIps;
	}

	public void setGroups(List<String> groups) {
		m_groups = groups;
	}

	public void setHitTrend(String hitTrend) {
		m_hitTrend = hitTrend;
	}

	public void setLineChart(String name, LineChart lineChart) {
		m_lineCharts.put(name, lineChart);
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setReport(TransactionReport report) {
		m_report = report;
	}

	public void setResponseTrend(String responseTrend) {
		m_responseTrend = responseTrend;
	}

	public void setTable(TableViewModel<?> table) {
		m_table = table;
	}

	public void setType(String type) {
		m_type = type;
	}
}
