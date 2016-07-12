package org.unidal.cat.spi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum ReportPeriod {
	HOUR(0) {
		@Override
		public Date getBaselineStartTime(Date date) {
			long time = date.getTime();

			time = time - time % (3600 * 1000L);
			time = time - 24 * 3600 * 1000L; // one day ago
			return new Date(time);
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMddHH";
		}

		@Override
		protected long getDuration() {
			return 3600 * 1000L;
		}

		/**
		 * Next hour
		 */
		@Override
		public Date getReduceTime(Date date) {
			long time = date.getTime();

			time = time - time % (3600 * 1000L);
			time += 3600 * 1000L;

			return new Date(time);
		}

		@Override
		public Date getStartTime(Date date) {
			long time = date.getTime();

			time = time - time % (3600 * 1000L);
			return new Date(time);
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
	},

	DAY(1) {
		@Override
		public Date getBaselineStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.MONTH, -1); // one month ago
			return cal.getTime();
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
		}

		@Override
		protected long getDuration() {
			return 24 * 3600 * 1000L;
		}

		/**
		 * 1 AM of next day
		 */
		@Override
		public Date getReduceTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
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
	},

	WEEK(2) {
		@Override
		public Date getBaselineStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_WEEK, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.DATE, -91); // about 3 months ago
			return cal.getTime();
		}

		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
		}

		@Override
		protected long getDuration() {
			return 7 * 24 * 3600 * 1000L;
		}

		/**
		 * 1 AM of first day of next week
		 */
		@Override
		public Date getReduceTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.add(Calendar.DATE, 7);
			cal.set(Calendar.DAY_OF_WEEK, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}

		@Override
		public Date getStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_WEEK, 1);
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
	},

	MONTH(3) {
		@Override
		public Date getBaselineStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.YEAR, -1); // one year ago
			return cal.getTime();
		}

		@Override
		public String getDateFormat() {
			return "yyyyMM";
		}

		@Override
		protected long getDuration() {
			return 30 * 24 * 3600 * 1000L;
		}

		@Override
		public Date getLastStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.MONTH, -1);

			return cal.getTime();
		}

		/**
		 * 1 AM of first day of next month
		 */
		@Override
		public Date getReduceTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}

		@Override
		public Date getStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.DAY_OF_MONTH, 1);
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
	},

	YEAR(4) {
		@Override
		public Date getBaselineStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.YEAR, -1); // one year ago
			return cal.getTime();
		}

		@Override
		public String getDateFormat() {
			return "yyyy";
		}

		@Override
		protected long getDuration() {
			return 365 * 24 * 3600 * 1000L;
		}

		@Override
		public Date getLastStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.YEAR, -1);

			return cal.getTime();
		}

		@Override
		public Date getReduceTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.add(Calendar.YEAR, 1);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}

		@Override
		public Date getStartTime(Date date) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
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

	public String format(Date date) {
		return new SimpleDateFormat(getDateFormat()).format(date);
	}

	public abstract Date getBaselineStartTime(Date date);

	protected abstract String getDateFormat();

	protected abstract long getDuration();

	public int getId() {
		return m_id;
	}

	public Date getLastStartTime(Date date) {
		Date startTime = getStartTime(date);
		long duration = getDuration();

		return new Date(startTime.getTime() - duration);
	}

	public String getName() {
		return name().toLowerCase();
	}

	public Date getNextStartTime(Date date) {
		Date startTime = getStartTime(date);
		long duration = getDuration();

		return new Date(startTime.getTime() + duration);
	}

	public abstract Date getReduceTime(Date date);

	public abstract Date getStartTime(Date date);

	public boolean isCurrent(Date date) {
		return getStartTime(date).getTime() == getStartTime(new Date()).getTime();
	}

	public abstract boolean isHistorical(Date startTime);

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
}
