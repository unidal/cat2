package org.unidal.cat.plugin.transactions.report.page;

public enum JspFile {
	VIEW("/jsp/plugin/transactions/transactions.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
