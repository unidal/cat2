package org.unidal.cat.plugin.transaction.report.page;

public enum JspFile {
	REPORT("/jsp/report/transaction.jsp"),

	HOURLY_GRAPH("/jsp/report/transaction-hourly-graph.jsp"),

	HISTORY_GRAPH("/jsp/report/transaction-history-graph.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
