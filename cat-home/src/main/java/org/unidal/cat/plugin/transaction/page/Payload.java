package org.unidal.cat.plugin.transaction.page;

import java.net.URLEncoder;
import java.util.Calendar;
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

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("date")
	private long m_startTime;

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
	public Date getHistoryEndDate() {
		return getReportPeriod().getNextStartTime(getStartTime());
	}

	@Override
	public Date getHistoryStartDate() {
		return getStartTime();
	}

	public Date getStartTime() {
		ReportPeriod period = getReportPeriod();

		if (m_startTime <= 0) {
			m_startTime = System.currentTimeMillis();
		}

		Date time = getDate(period, m_startTime, m_step);
		Date startTime = period.getStartTime(time);

		if (startTime.after(new Date())) {
			return period.getStartTime(new Date());
		} else {
			return startTime;
		}
	}

	private Date getDate(ReportPeriod period, long date, int step) {
		long time = date < 100000000L ? date * 100 : date;
		long year = (time % 10000000000L) / 1000000L;
		long month = (time % 1000000L) / 10000L;
		long day = (time % 10000L) / 100L;
		long hour = (time % 100L) / 1L;
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, (int) year);
		cal.set(Calendar.MONTH, (int) month - 1);
		cal.set(Calendar.DATE, (int) day);
		cal.set(Calendar.HOUR_OF_DAY, (int) hour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (step != 0) {
			switch (period) {
			case HOUR:
				cal.add(Calendar.HOUR_OF_DAY, step);
				break;
			case DAY:
				cal.add(Calendar.DATE, step);
				break;
			case WEEK:
				cal.add(Calendar.DATE, 7 * step);
				break;
			case MONTH:
				cal.add(Calendar.MONTH, step);
				break;
			}
		}

		return cal.getTime();
	}
}
