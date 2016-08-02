package org.unidal.cat.spi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum ReportPeriod {
	HOUR(0) {
		@Override
		protected void add(Calendar cal, int step) {
			cal.add(Calendar.HOUR, step);
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMddHH";
		}

		@Override
		protected Calendar getStartCalendar(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal;
		}

		@Override
		public boolean isHistorical(Date startTime) {
			long now = System.currentTimeMillis();

			// latest 2 hours
			if (now - startTime.getTime() < 2 * 3600 * 1000L) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		protected void setBaselineCalendar(Calendar cal) {
			cal.add(Calendar.DATE, -1); // one day ago
		}

		/**
		 * One hour later
		 */
		@Override
		protected void setReducerCalendar(Calendar cal) {
			cal.add(Calendar.HOUR, 1);
		}
	},

	DAY(1) {
		@Override
		protected void add(Calendar cal, int step) {
			cal.add(Calendar.DATE, step);
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
		}

		@Override
		protected Calendar getStartCalendar(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal;
		}

		@Override
		public Date getStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}

		@Override
		public boolean isHistorical(Date startTime) {
			return true;
		}

		@Override
		protected void setBaselineCalendar(Calendar cal) {
			cal.add(Calendar.DATE, -7); // one week ago
		}

		/**
		 * 1 AM of next day
		 */
		@Override
		protected void setReducerCalendar(Calendar cal) {
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
		}
	},

	WEEK(2) {
		@Override
		protected void add(Calendar cal, int step) {
			cal.add(Calendar.DATE, 7 * step);
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
		}

		@Override
		protected Calendar getStartCalendar(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_WEEK, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal;
		}

		@Override
		public boolean isHistorical(Date startTime) {
			return true;
		}

		@Override
		protected void setBaselineCalendar(Calendar cal) {
			cal.add(Calendar.DATE, -14); // 2 weeks ago
		}

		/**
		 * 1 AM of first day of next week
		 */
		@Override
		protected void setReducerCalendar(Calendar cal) {
			cal.add(Calendar.DATE, 7);
			cal.set(Calendar.DAY_OF_WEEK, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
		}
	},

	MONTH(3) {
		@Override
		protected void add(Calendar cal, int step) {
			cal.add(Calendar.MONTH, step);
		}

		@Override
		public String getDateFormat() {
			return "yyyyMM";
		}

		@Override
		protected Calendar getStartCalendar(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal;
		}

		@Override
		public boolean isHistorical(Date startTime) {
			return true;
		}

		@Override
		protected void setBaselineCalendar(Calendar cal) {
			cal.add(Calendar.YEAR, -1); // one year ago
		}

		/**
		 * 2 AM of first day of next month
		 */
		@Override
		protected void setReducerCalendar(Calendar cal) {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 2);
		}
	},

	YEAR(4) {
		@Override
		protected void add(Calendar cal, int step) {
			cal.add(Calendar.YEAR, step);
		}

		@Override
		public String getDateFormat() {
			return "yyyy";
		}

		@Override
		protected Calendar getStartCalendar(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal;
		}

		@Override
		public boolean isHistorical(Date startTime) {
			return true;
		}

		@Override
		protected void setBaselineCalendar(Calendar cal) {
			cal.add(Calendar.YEAR, -2); // 2 years ago
		}

		/**
		 * 3 AM of first day of next January
		 */
		@Override
		protected void setReducerCalendar(Calendar cal) {
			cal.add(Calendar.YEAR, 1);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 3);
		}
	};

	private int m_id;

	private ReportPeriod(int id) {
		m_id = id;
	}

	public static ReportPeriod getById(int id, ReportPeriod defaultValue) {
		for (ReportPeriod period : values()) {
			if (period.getId() == id) {
				return period;
			}
		}

		return defaultValue;
	}

	public static ReportPeriod getByName(String name, ReportPeriod defaultValue) {
		for (ReportPeriod period : values()) {
			if (period.name().equalsIgnoreCase(name)) {
				return period;
			}
		}

		return defaultValue;
	}

	protected abstract void add(Calendar cal, int step);

	public String format(Date date) {
		return new SimpleDateFormat(getDateFormat()).format(date);
	}

	public Date getBaselineStartTime(Date date) {
		Calendar cal = getStartCalendar(date);

		setBaselineCalendar(cal);
		return cal.getTime();
	}

	protected abstract String getDateFormat();

	public Date getEndTime(Date date) {
		Calendar cal = getStartCalendar(date);

		add(cal, 1);
		cal.add(Calendar.MILLISECOND, -1);

		return cal.getTime();
	}

	public int getId() {
		return m_id;
	}

	public Date getLastStartTime(Date date) {
		Calendar cal = getStartCalendar(date);

		add(cal, -1);

		return cal.getTime();
	}

	public String getName() {
		return name().toLowerCase();
	}

	public Date getNextStartTime(Date date) {
		Calendar cal = getStartCalendar(date);

		add(cal, 1);

		return cal.getTime();
	}

	public Date getReduceTime(Date date) {
		Calendar cal = getStartCalendar(date);

		setReducerCalendar(cal);
		return cal.getTime();
	}

	protected abstract Calendar getStartCalendar(Date date);

	public Date getStartTime(Date date) {
		return getStartCalendar(date).getTime();
	}

	public boolean isCurrent(Date date) {
		return getStartTime(date).getTime() == getStartTime(new Date()).getTime();
	}

	public boolean isDay() {
		return this == DAY;
	}

	public abstract boolean isHistorical(Date startTime);

	public boolean isHour() {
		return this == HOUR;
	}

	public Date parse(String date, Date defaultValue) {
		if (date != null) {
			try {
				return new SimpleDateFormat(getDateFormat()).parse(date);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	protected abstract void setBaselineCalendar(Calendar cal);

	protected abstract void setReducerCalendar(Calendar cal);
}
