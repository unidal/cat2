package org.unidal.cat.spi.report.task;

public enum ReportTaskStatus {
	TODO(1),

	DOING(2),

	DONE(3),

	FAILED(9);

	private int m_id;

	private ReportTaskStatus(int id) {
		m_id = id;
	}

	public static ReportTaskStatus getById(int id, ReportTaskStatus defaultValue) {
		for (ReportTaskStatus status : values()) {
			if (status.getId() == id) {
				return status;
			}
		}

		return defaultValue;
	}

	public int getId() {
		return m_id;
	}
}
