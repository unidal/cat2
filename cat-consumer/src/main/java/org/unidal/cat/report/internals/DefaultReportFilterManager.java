package org.unidal.cat.report.internals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportFilterManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportFilterManager.class)
public class DefaultReportFilterManager extends ContainerHolder implements ReportFilterManager {
	private Map<String, ReportFilter<Report>> m_filters = new HashMap<String, ReportFilter<Report>>();

	private Set<String> m_missingFilters = new HashSet<String>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportFilter<T> getFilter(String reportName, String id) {
		if (id == null) {
			return null;
		}

		String key = reportName + ":" + id;
		ReportFilter<Report> filter = m_filters.get(key);

		if (filter == null) {
			if (m_missingFilters.contains(key)) {
				return null;
			}

			synchronized (m_filters) {
				filter = (ReportFilter<Report>) m_filters.get(key);

				if (filter == null) {
					try {
						filter = lookup(ReportFilter.class, key);

						m_filters.put(key, filter);
					} catch (Exception e) {
						Cat.logError(String.format("ReportFilter(%s) is missing, IGNORED.", key), e);
						m_missingFilters.add(key);
					}
				}
			}
		}

		return (ReportFilter<T>) filter;
	}
}
