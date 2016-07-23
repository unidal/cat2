package org.unidal.cat.plugin.transaction.page;

public enum JspFile {
	REPORT("/jsp/plugin/transaction/transaction.jsp"),

	HOURLY_GRAPH("/jsp/plugin/transaction/transaction-hourly-graph.jsp"),

	HISTORY_GRAPH("/jsp/plugin/transaction/transaction-history-graph.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
