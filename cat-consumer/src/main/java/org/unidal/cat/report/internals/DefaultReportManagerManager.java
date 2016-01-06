package org.unidal.cat.report.internals;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.report.ReportManagerManager;
import org.unidal.cat.report.spi.ReportManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

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
@Named(type = ReportManagerManager.class)
public class DefaultReportManagerManager extends ContainerHolder implements ReportManagerManager {
	private Map<String, ReportManager<?>> m_cachedManagers = new HashMap<String, ReportManager<?>>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends ReportManager<?>> T getReportManager(String id) {
		T manager = (T) m_cachedManagers.get(id);

		if (manager == null) {
			try {
				manager = (T) lookup(ReportManager.class, id);

				m_cachedManagers.put(id, manager);
			} catch (RuntimeException e) {
				throw new IllegalStateException(String.format("No ReportManager(%s) defined!", id), e);
			}
		}

		return manager;
	}

	@Override
	public boolean hasReportManager(String id) {
		ReportManager<?> manager = m_cachedManagers.get(id);

		if (manager == null) {
			try {
				manager = lookup(ReportManager.class, id);

				m_cachedManagers.put(id, manager);
			} catch (RuntimeException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}
}
