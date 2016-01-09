package com.dianping.cat.report.page.transaction;

import java.net.URLEncoder;
import java.util.Date;

import org.unidal.cat.report.ReportPeriod;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("queryname")
	private String m_queryName;

	@FieldMeta("sort")
	private String m_sortBy;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("xml")
	private boolean m_xml;

	@FieldMeta("group")
	private String m_group;

	public Payload() {
		super(ReportPage.TRANSACTION);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getEncodedType() {
		try {
			return URLEncoder.encode(m_type, "utf-8");
		} catch (Exception e) {
			return m_type;
		}
	}

	public String getGroup() {
		return m_group;
	}

	public String getName() {
		return m_name;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public ReportPeriod getReportPeriod() {
		if (m_action == null || !m_action.isHistory()) {
			return ReportPeriod.HOUR;
		} else {
			String type = super.getReportType();
			ReportPeriod period = ReportPeriod.getByName(type, ReportPeriod.DAY);

			return period;
		}
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public String getType() {
		return m_type;
	}

	public boolean isXml() {
		return m_xml;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TRANSACTION);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}

		if (m_type != null && m_type.length() == 0) {
			m_type = null;
		}

		if (m_queryName != null && m_queryName.length() == 0) {
			m_queryName = null;
		}
	}

	@Override
   public Date getHistoryStartDate() {
	   return ReportPeriod.DAY.getStartTime(new Date()); // TODO hack
   }
}
