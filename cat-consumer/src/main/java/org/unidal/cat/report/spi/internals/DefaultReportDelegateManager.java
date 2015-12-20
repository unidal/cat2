package org.unidal.cat.report.spi.internals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.ReportDelegateManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportDelegateManager.class)
public class DefaultReportDelegateManager extends ContainerHolder implements ReportDelegateManager {
	private Map<String, ReportDelegate<Report>> m_filters = new HashMap<String, ReportDelegate<Report>>();

	private Set<String> m_missingFilters = new HashSet<String>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportDelegate<T> getDelegate(String reportName) {
		String key = reportName;
		ReportDelegate<Report> filter = m_filters.get(key);

		if (filter == null) {
			if (m_missingFilters.contains(key)) {
				return null;
			}

			synchronized (m_filters) {
				filter = (ReportDelegate<Report>) m_filters.get(key);

				if (filter == null) {
					try {
						filter = lookup(ReportDelegate.class, key);

						m_filters.put(key, filter);
					} catch (Exception e) {
						Cat.logError(String.format("ReportDelegate(%s) is missing, IGNORED.", key), e);
						m_missingFilters.add(key);
					}
				}
			}
		}

		return (ReportDelegate<T>) filter;
	}
}
