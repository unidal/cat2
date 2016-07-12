package org.unidal.cat.plugin.problem.page;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

import java.util.Calendar;
import java.util.Date;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("groupName")
	private String m_groupName;

	@FieldMeta("linkCount")
	private int m_linkCount;

	@FieldMeta("urlThreshold")
	private int m_urlThreshold = 1000;

	@FieldMeta("minute")
	private int m_minute;

	@FieldMeta("sqlThreshold")
	private int m_sqlThreshold = 100;

	@FieldMeta("serviceThreshold")
	private int m_serviceThreshold = 50;

	@FieldMeta("cacheThreshold")
	private int m_cacheThreshold = 10;

	@FieldMeta("callThreshold")
	private int m_callThreshold = 50;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("date")
	private long m_startTime;

	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getCacheThreshold() {
		return m_cacheThreshold;
	}

	public int getCallThreshold() {
		return m_callThreshold;
	}

	public String getGroup() {
		return m_group;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public int getLinkCount() {
		if (m_linkCount < 40) {
			m_linkCount = 40;
		}
		return m_linkCount;
	}

	public int getMinute() {
		return m_minute;
	}

	public String getQueryString() {
		StringBuilder sb = new StringBuilder();

		sb.append("&urlThreshold=").append(m_urlThreshold);
		sb.append("&sqlThreshold=").append(m_sqlThreshold);
		sb.append("&serviceThreshold=").append(m_serviceThreshold);
		sb.append("&cacheThreshold=").append(m_cacheThreshold);
		sb.append("&callThreshold=").append(m_callThreshold);
		return sb.toString();
	}

	public int getServiceThreshold() {
		return m_serviceThreshold;
	}

	public int getSqlThreshold() {
		return m_sqlThreshold;
	}

	public String getStatus() {
		return m_status;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public String getType() {
		return m_type;
	}

	public int getUrlThreshold() {
		return m_urlThreshold;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	public void setCacheThreshold(int cacheThreshold) {
		m_cacheThreshold = cacheThreshold;
	}

	public void setCallThreshold(int callThreshold) {
		m_callThreshold = callThreshold;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setLinkCount(int linkSize) {
		m_linkCount = linkSize;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.PROBLEM);
	}

	public void setServiceThreshold(int serviceThreshold) {
		m_serviceThreshold = serviceThreshold;
	}

	public void setSqlThreshold(int sqlThreshold) {
		m_sqlThreshold = sqlThreshold;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setUrlThreshold(int longTime) {
		m_urlThreshold = longTime;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
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

	public Date getStartTime() {
		ReportPeriod period = getReportPeriod();
		Date startTime;

		if (m_startTime <= 0) {
			startTime = period.getStartTime(new Date());
		} else {
			Date time = getDate(period, m_startTime, m_step);

			startTime = period.getStartTime(time);
		}

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
				case YEAR:
					cal.add(Calendar.YEAR, step);
					break;
			}
		}

		return cal.getTime();
	}
}
