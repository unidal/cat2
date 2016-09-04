package org.unidal.cat.plugin.transaction.config.page;

public enum JspFile {
	VIEW("/jsp/config/transaction.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
