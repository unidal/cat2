package org.unidal.cat.plugin.transaction.report.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.report.ReportPage;
import org.unidal.cat.plugin.transaction.view.GraphViewModel;
import org.unidal.cat.plugin.transaction.view.LineChart;
import org.unidal.cat.plugin.transaction.view.TableViewModel;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;

@ModelMeta(TransactionConstants.NAME)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private List<String> m_groups;

	private List<String> m_groupIps;

	private String m_queryName;

	@EntityMeta
	private TransactionReport m_report;

	private String m_type;

	// cat2
	private TableViewModel<?> m_table;

	private GraphViewModel m_graph;

	private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.REPORT;
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

	public GraphViewModel getGraph() {
		return m_graph;
	}

	public List<String> getGroupIps() {
		return m_groupIps;
	}

	public List<String> getGroups() {
		return m_groups;
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

	public String getQueryName() {
		return m_queryName;
	}

	public TransactionReport getReport() {
		return m_report;
	}

	public TableViewModel<?> getTable() {
		return m_table;
	}

	public String getType() {
		return m_type;
	}

	public void setGraph(GraphViewModel graph) {
		m_graph = graph;
	}

	public void setGroupIps(List<String> groupIps) {
		m_groupIps = groupIps;
	}

	public void setGroups(List<String> groups) {
		m_groups = groups;
	}

	public void setLineChart(String name, LineChart lineChart) {
		m_lineCharts.put(name, lineChart);
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setReport(TransactionReport report) {
		m_report = report;
	}

	public void setTable(TableViewModel<?> table) {
		m_table = table;
	}

	public void setType(String type) {
		m_type = type;
	}
}
