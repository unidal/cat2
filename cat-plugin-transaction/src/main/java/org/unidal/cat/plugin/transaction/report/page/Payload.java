package org.unidal.cat.plugin.transaction.report.page;

import java.net.URLEncoder;

import org.unidal.cat.core.report.page.CoreReportPayload;
import org.unidal.cat.plugin.transaction.report.ReportPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;

public class Payload extends CoreReportPayload<ReportPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("query")
	private String m_query;

	@FieldMeta("sort")
	private String m_sortBy;

	public Payload() {
		super(ReportPage.TRANSACTION);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getEncodedType() {
		try {
			return URLEncoder.encode(m_type, "utf-8");
		} catch (Exception e) {
			return m_type;
		}
	}

	public String getIp() {
		return m_ip;
	}

	public String getName() {
		return m_name;
	}

	public String getQuery() {
		return m_query;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.REPORT);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		super.validate(ctx);

		m_action = denull(m_action, Action.REPORT);
		m_domain = denull(m_domain, Constants.CAT);
		m_ip = denull(m_ip, Constants.ALL);
		m_type = denull(m_type, null);
		m_name = denull(m_name, null);
		m_query = denull(m_query, null);
		m_sortBy = denull(m_sortBy, "avg");
	}
}
