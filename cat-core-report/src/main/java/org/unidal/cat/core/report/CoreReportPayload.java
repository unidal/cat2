package org.unidal.cat.core.report;

import java.util.Calendar;
import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public abstract class CoreReportPayload<P extends Page, A extends Action> implements ActionPayload<P, A> {
	private P m_page;

	@FieldMeta("period")
	private ReportPeriod m_period = ReportPeriod.HOUR;

	@FieldMeta("date")
	private long m_date;

	@FieldMeta("step")
	private int m_step;

	private Date m_startTime;

	private Date m_endTime;

	public CoreReportPayload(P page) {
		m_page = page;
	}

	protected Date buildDate(ReportPeriod period, long date, int step) {
		long time = date < 100000000L ? date * 100 : date; // yyyyMMddhh
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

	protected Date buildStartTime() {
		ReportPeriod period = getPeriod();
		Date startTime;

		if (m_date <= 0) {
			startTime = period.getStartTime(new Date());

			if (!period.isHour()) {
				startTime = period.getLastStartTime(startTime);
			}
		} else {
			Date date = buildDate(period, m_date, m_step);

			startTime = period.getStartTime(date);
		}

		return startTime;
	}

	protected <T> T denull(T str, T defaultValue) {
		if (str == null) {
			return defaultValue;
		} else if (str instanceof String) {
			if (((String) str).length() == 0) {
				return defaultValue;
			}
		}

		return str;
	}

	public Date getEndTime() {
		return m_endTime;
	}

	// for JSP
	public String getFormattedEndTime() {
		return m_period.format(m_endTime);
	}

	// for JSP
	public String getFormattedStartTime() {
		return m_period.format(m_startTime);
	}

	@Override
	public P getPage() {
		return m_page;
	}

	public ReportPeriod getPeriod() {
		return m_period;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	@Override
	public void setPage(String page) {
		// ignore it
	}

	public void setPeriod(String period) {
		m_period = ReportPeriod.getByName(period, ReportPeriod.HOUR);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		m_startTime = buildStartTime();
		m_endTime = m_period.getEndTime(m_startTime);
	}
}
