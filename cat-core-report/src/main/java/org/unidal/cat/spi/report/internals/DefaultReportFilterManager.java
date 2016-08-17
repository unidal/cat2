package org.unidal.cat.spi.report.internals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.ReportFilterManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportFilterManager.class)
public class DefaultReportFilterManager extends ContainerHolder implements ReportFilterManager {
	private Map<String, ReportFilter<Report>> m_filters = new HashMap<String, ReportFilter<Report>>();

	private Set<String> m_badFilters = new HashSet<String>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportFilter<T> getFilter(String reportName, String id) {
		if (id == null) {
			return null;
		}

		String key = reportName + ":" + id;
		ReportFilter<Report> filter = m_filters.get(key);

		if (filter == null) {
			if (m_badFilters.contains(key)) {
				return null;
			}

			synchronized (m_filters) {
				filter = (ReportFilter<Report>) m_filters.get(key);

				if (filter == null) {
					try {
						filter = lookup(ReportFilter.class, key);

						m_filters.put(key, filter);
					} catch (Exception e) {
						Cat.logError(String.format("ReportFilter(%s) is missing or invalid, IGNORED.", key), e);
						m_badFilters.add(key);
					}
				}
			}
		}

		return (ReportFilter<T>) filter;
	}
}
