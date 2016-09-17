package org.unidal.cat.plugin.event.report.page;

public enum JspFile {
	REPORT("/jsp/report/event.jsp"),

	HOURLY_GRAPH("/jsp/report/event-hourly-graph.jsp"),

	HISTORY_GRAPH("/jsp/report/event-history-graph.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
