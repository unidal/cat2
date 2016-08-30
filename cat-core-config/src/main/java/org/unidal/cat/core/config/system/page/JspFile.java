package org.unidal.cat.core.config.system.page;

public enum JspFile {
	VIEW("/jsp/system/config.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
