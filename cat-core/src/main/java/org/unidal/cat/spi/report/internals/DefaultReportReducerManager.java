package org.unidal.cat.spi.report.internals;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.cat.spi.report.ReportReducerManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportReducerManager.class)
public class DefaultReportReducerManager extends ContainerHolder implements ReportReducerManager {
	private Map<String, ReportReducer<Report>> m_reducers = new HashMap<String, ReportReducer<Report>>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportReducer<T> getReducer(String reportName, String id) {
		String key = reportName + ":" + id;
		ReportReducer<Report> reducer = m_reducers.get(key);

		if (reducer == null) {
			synchronized (m_reducers) {
				reducer = (ReportReducer<Report>) m_reducers.get(key);

				if (reducer == null) {
					reducer = lookup(ReportReducer.class, key);
					m_reducers.put(key, reducer);
				}
			}
		}

		return (ReportReducer<T>) reducer;
	}
}
