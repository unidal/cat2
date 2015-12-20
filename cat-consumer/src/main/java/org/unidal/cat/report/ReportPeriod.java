package org.unidal.cat.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public enum ReportPeriod {
	HOUR {
		@Override
		public String getDateFormat() {
			return "yyyyMMddHH";
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

	DAY {
		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
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

	WEEK {
		@Override
		public String getDateFormat() {
			return "yyyyMMdd";
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

	MONTH {
		@Override
		public String getDateFormat() {
			return "yyyyMM";
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
	};

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

	public abstract String getDateFormat();

	public String getName() {
		return name().toLowerCase();
	}

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
