package org.unidal.cat.plugin.event.page;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/event/event.jsp"),

    HOURLY_GRAPH("/jsp/report/event/eventGraphs.jsp"),

    HISTORY_REPORT("/jsp/report/event/eventHistoryReport.jsp"),

    HISTORY_GRAPH("/jsp/report/event/eventHistoryGraphs.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
