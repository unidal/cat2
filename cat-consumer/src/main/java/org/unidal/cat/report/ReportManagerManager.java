package org.unidal.cat.report;

import org.unidal.cat.report.spi.ReportManager;

/**
 * <ul>
 * Following use scenarios of report are considered:
 * <li>Life cycle management, including report creation, storage, and fetch with caching</li>
 * <li>Batch aggregation for period(hourly/daily/weekly/monthly etc.) with unit(minute/5-minute/15-minute/hour/day etc.)</li>
 * <li>Real-time aggregation from remote processes with filtering and compression</li>
 * <li>Easy to develop/configure new report</li>
 * <li>Support report with dependent reports</li>
 * <li>Friendly and high-performance service API</li>
 * </ul>
 */
public interface ReportManagerManager {
	public <T extends ReportManager<?>> T getReportManager(String id);

	public boolean hasReportManager(String id);
}
