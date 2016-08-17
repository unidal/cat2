package org.unidal.cat.spi.report.internals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportDelegateManager.class)
public class DefaultReportDelegateManager extends ContainerHolder implements ReportDelegateManager {
	private Map<String, ReportDelegate<Report>> m_delegates = new HashMap<String, ReportDelegate<Report>>();

	private Set<String> m_missingDelegates = new HashSet<String>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportDelegate<T> getDelegate(String reportName) {
		String key = reportName;
		ReportDelegate<Report> delegate = m_delegates.get(key);

		if (delegate == null) {
			if (m_missingDelegates.contains(key)) {
				return null;
			}

			synchronized (m_delegates) {
				delegate = (ReportDelegate<Report>) m_delegates.get(key);

				if (delegate == null) {
					try {
						delegate = lookup(ReportDelegate.class, key);

						m_delegates.put(key, delegate);
					} catch (Exception e) {
						Cat.logError(String.format("ReportDelegate(%s) is missing, IGNORED.", key), e);
						m_missingDelegates.add(key);
					}
				}
			}
		}

		return (ReportDelegate<T>) delegate;
	}
}
