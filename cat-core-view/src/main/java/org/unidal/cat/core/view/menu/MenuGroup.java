package org.unidal.cat.core.view.menu;

public enum MenuGroup {
	REALTIME("realtime", "Realtime", "fa fa-signal", "btn btn-success", "/r/t"),

	OFFLINE("offline", "Offline", "fa fa-film", "btn btn-grey", "/r/statistics"),

	DOCUMENT("document", "Document", "fa fa-users", "btn btn-warning", "/doc"),

	CONFIG("config", "Config", "fa fa-cogs", "btn btn-danger", "/system/config");

	private String m_id;

	private String m_title;

	private String m_styleClasses;

	private String m_backgroundStyleClasses;

	private String m_link;

	private MenuGroup(String id, String title, String styleClasses, String backgroundStyleClasses, String link) {
		m_id = id;
		m_title = title;
		m_styleClasses = styleClasses;
		m_backgroundStyleClasses = backgroundStyleClasses;
		m_link = link;
	}

	public String getBackgroundStyleClasses() {
		return m_backgroundStyleClasses;
	}

	public String getId() {
		return m_id;
	}

	public String getLink() {
		return m_link;
	}

	public String getStyleClasses() {
		return m_styleClasses;
	}

	public String getTitle() {
		return m_title;
	}
}
