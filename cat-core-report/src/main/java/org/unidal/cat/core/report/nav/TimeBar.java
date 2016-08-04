package org.unidal.cat.core.report.nav;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.spi.ReportPeriod;

public enum TimeBar {
	SEVEN_DAY_BEFORE("hourly", "-7d", -24 * 7),

	ONE_DAY_BEFORE("hourly", "-1d", -24),

	TWO_HOURS_BEFORE("hourly", "-2h", -2),

	ONE_HOUR_BEFORE("hourly", "-1h", -1),

	ONE_HOUR_LATER("hourly", "+1h", 1),

	TWO_HOURS_LATER("hourly", "+2h", 2),

	ONE_DAY_LATER("hourly", "+1d", 24),

	SEVEN_DAY_LATER("hourly", "+7d", 24 * 7),

	MONTH("history", "month", "-1m", "+1m"),

	WEEK("history", "week", "-1w", "+1w"),

	DAY("history", "day", "-1d", "+1d");

	private String m_type;

	private String m_title;

	private int m_step;

	private String m_last;

	private String m_next;

	private TimeBar(String type, String title, int step) {
		m_type = type;
		m_title = title;
		m_step = step;
	}

	private TimeBar(String type, String title, String last, String next) {
		m_type = type;
		m_title = title;
		m_last = last;
		m_next = next;
	}

	public static TimeBar getByPeriod(ReportPeriod period) {
		switch (period) {
		case DAY:
			return DAY;
		case WEEK:
			return WEEK;
		case MONTH:
			return MONTH;
		default:
			return null;
		}
	}

	public static TimeBar getByType(String type) {
		for (TimeBar nav : values()) {
			if (nav.getType().equalsIgnoreCase(type)) {
				return nav;
			}
		}

		return null;
	}

	public static List<TimeBar> getHistoryBars() {
		List<TimeBar> bars = new ArrayList<TimeBar>();

		for (TimeBar bar : values()) {
			if (bar.getType().equals("history")) {
				bars.add(bar);
			}
		}

		return bars;
	}

	public static List<TimeBar> getHourlyBars() {
		List<TimeBar> bars = new ArrayList<TimeBar>();

		for (TimeBar bar : values()) {
			if (bar.getType().equals("hourly")) {
				bars.add(bar);
			}
		}

		return bars;
	}

	public String getLast() {
		return m_last;
	}

	public String getNext() {
		return m_next;
	}

	public int getStep() {
		return m_step;
	}

	public String getTitle() {
		return m_title;
	}

	public String getType() {
		return m_type;
	}
}
