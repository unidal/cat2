package org.unidal.cat.document.spi;

import java.util.ArrayList;
import java.util.List;

public enum Document {
	INDEX("index", "Index", "glyphicon glyphicon-home", false) {
		{
			defineFeature("overview", "Overview");
			defineFeature("customer", "Customer");
		}
	},

	RELEASE("release", "Release", "glyphicon glyphicon-book", false) {
		{
			defineFeature("release", "Release");
		}
	},

	DEPLOY("deployment", "Deployment", "fa fa-cogs", false) {
		{
			defineFeature("production", "Production");
		}
	},

	USER("user", "User Guide", "fa fa-users", true) {
		{
			defineFeature("overall", "Overall");
			defineFeature("logview", "Logview");
		}
	},

	CLIENT("client", "Client Side", "fa fa-globe", true) {
		{
			defineFeature("app", "Mobile App");
			defineFeature("web", "Web App");
		}
	},

	ALERT("alert", "Alerting", "fa fa-bell", true) {
		{
			defineFeature("overall", "Overall");
		}
	},

	INSTRUMENT("instrument", "Instrument", "fa fa-cutlery", false) {
		{
			defineFeature("instrument", "Instrument");
		}
	},

	API("api", "API Reference", "glyphicon glyphicon-align-left", true) {
		{

		}
	},

	DEVELOPER("developer", "Developer", "glyphicon glyphicon-refresh", false) {
		{
			defineFeature("developer", "Developer");
		}
	},

	ARCHITECTURE("architecture", "Architecture", "fa fa-book", false) {
		{
			defineFeature("architecture", "Architecture");
		}
	},

	FAQ("faq", "FAQ", "fa fa-inbox", false) {
		{
			defineFeature("faq", "FAQ");
		}
	},

	PLUGIN("plugin", "Plugin", "fa fa-key", false) {
		{
			defineFeature("plugin", "Plugin");
		}
	};

	private String m_id;

	private String m_title;

	private String m_styleClasses;

	private List<DocumentFeature> m_features = new ArrayList<DocumentFeature>();

	private boolean m_tabbed;

	private Document(String id, String title, String styleClasses, boolean tabbed) {
		m_id = id;
		m_title = title;
		m_styleClasses = styleClasses;
		m_tabbed = tabbed;
	}

	public static Document getById(String id, Document defaultValue) {
		for (Document doc : values()) {
			if (doc.getId().equals(id)) {
				return doc;
			}
		}

		return defaultValue;
	}

	public boolean isTabbed() {
		return m_tabbed;
	}

	protected void defineFeature(String id, String title) {
		String url = String.format("/jsp/document/%s/%s.jsp", m_id, id);

		m_features.add(new DocumentFeature(id, title, url));
	}

	public List<DocumentFeature> getFeatures() {
		return m_features;
	}

	public String getId() {
		return m_id;
	}

	public String getStyleClasses() {
		return m_styleClasses;
	}

	public String getTitle() {
		return m_title;
	}

	public void register(DocumentFeature feature) {
		if (!m_features.contains(feature)) {
			m_features.add(feature);
		}

		sortFeatures();
	}

	private void sortFeatures() {
		// TODO
	}
}
