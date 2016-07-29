package org.unidal.cat.core.report.nav;

public enum NavigatorBar {
	SEVEN_DAY_BEFORE("hour", "-7d", -24 * 7),

	ONE_DAY_BEFORE("hour", "-1d", -24),

	TWO_HOURS_BEFORE("hour", "-2h", -2),

	ONE_HOUR_BEFORE("hour", "-1h", -1),

	ONE_HOUR_LATER("hour", "+1h", 1),

	TWO_HOURS_LATER("hour", "+2h", 2),

	ONE_DAY_LATER("hour", "+1d", 24),

	SEVEN_DAY_LATER("hour", "+7d", 24 * 7),

	MONTH("history", "month", "-1m", "+1m"),

	WEEK("history", "week", "-1w", "+1w"),

	DAY("history", "day", "-d", "+d");

	private String m_type;

	private String m_title;

	private int m_step;

	private String m_last;

	private String m_next;

	private NavigatorBar(String type, String title, int step) {
		m_type = type;
		m_title = title;
		m_step = step;
	}

	private NavigatorBar(String type, String title, String last, String next) {
		m_type = type;
		m_title = title;
		m_last = last;
		m_next = next;
	}
	
	public static NavigatorBar getByType(String type) {
		for (NavigatorBar nav : values()) {
			if (nav.getType().equalsIgnoreCase(type)) {
				return nav;
			}
		}

		return null;
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
