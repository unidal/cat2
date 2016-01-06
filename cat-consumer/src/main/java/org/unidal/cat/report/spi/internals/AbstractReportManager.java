package org.unidal.cat.report.spi.internals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportFilterManager;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.internals.FileReportStorage;
import org.unidal.cat.report.internals.ReportProvider;
import org.unidal.cat.report.internals.ReportStorage;
import org.unidal.cat.report.internals.ReportStoragePolicy;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.ReportDelegateManager;
import org.unidal.cat.report.spi.ReportManager;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractReportManager<T extends Report> implements ReportManager<T>, RoleHintEnabled {
	@Inject
	private ReportProvider<T> m_provider;

	@Inject
	private ReportStorage<T> m_storage;

	@Inject(FileReportStorage.ID)
	private ReportStorage<T> m_fileStorage;

	@Inject
	private ReportDelegateManager m_delegateManager;

	@Inject
	private ReportFilterManager m_filterManager;

	private String m_reportName;

	private Map<Long, ConcurrentMap<String, T>> m_reports = new HashMap<Long, ConcurrentMap<String, T>>();

	@Override
	public void doCheckpoint(Date startTime, int index, boolean atEnd) throws IOException {
		long key = startTime.getTime() + index;
		ConcurrentMap<String, T> map = m_reports.get(key);

		if (map != null && map.size() > 0) {
			List<T> reports = new ArrayList<T>(map.values());

			for (T report : reports) {
				if (atEnd) {
					m_storage.store(getDelegate(), ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);
				} else {
					m_storage.store(getDelegate(), ReportPeriod.HOUR, report, ReportStoragePolicy.FILE);
				}
			}
		}
	}

	@Override
	public void doInitLoad(Date startTime, int index) throws IOException {
		long key = startTime.getTime() + index;
		ConcurrentHashMap<String, T> map = new ConcurrentHashMap<String, T>();
		List<T> reports = m_storage.loadAll(getDelegate(), ReportPeriod.HOUR, startTime, null);

		for (T report : reports) {
			map.put(report.getDomain(), report);
		}

		m_reports.putIfAbsent(key, map);
	}

	@Override
	public void enableRoleHint(String roleHint) {
		m_reportName = roleHint;
	}

	@Override
	public T getCurrentReport(String domain, Date startTime, int index, boolean createIfNotExist) {
		long key = startTime.getTime() + index;
		ConcurrentMap<String, T> map = m_reports.get(key);

		if (map == null) {
			synchronized (m_reports) {
				map = m_reports.get(key);

				if (map == null) {
					map = new ConcurrentHashMap<String, T>();

					m_reports.put(key, map);
				}
			}
		}

		T report = map.get(domain);

		if (report == null) {
			report = getDelegate().createLocal(ReportPeriod.HOUR, domain, startTime);

			T r = map.putIfAbsent(domain, report);

			if (r != null) {
				report = r;
			}
		}

		return report;
	}

	protected ReportDelegate<T> getDelegate() {
		return m_delegateManager.getDelegate(m_reportName);
	}

	@Override
	public List<T> getLocalReports(ReportPeriod period, Date startTime, String domain) throws IOException {
		if (period == ReportPeriod.HOUR && period.isCurrent(startTime)) {
			int count = getThreadsCount();
			List<T> reports = new ArrayList<T>(count);

			for (int i = 0; i < count; i++) {
				T report = getCurrentReport(domain, startTime, i, false);

				if (report != null) {
					reports.add(report);
				}
			}
			return reports;
		} else {
			return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
		}
	}

	@Override
	public T getHourlyReport(Date startTime, String domain, String filterId, String... keyValuePairs) throws IOException {
		ReportDelegate<T> delegate = getDelegate();
		ReportFilter<? extends Report> filter = m_filterManager.getFilter(delegate.getName(), filterId);
		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), domain, startTime, ReportPeriod.HOUR, filter);

		int len = keyValuePairs.length;

		if (len % 2 == 0) {
			for (int i = 0; i < len; i += 2) {
				String property = keyValuePairs[i];
				String value = keyValuePairs[i + 1];

				ctx.setProperty(property, value);
			}
		} else {
			throw new IllegalArgumentException("Parameter(keyValuePairs) is not paired!");
		}

		T report = m_provider.getReport(ctx, delegate);

		return report;
	}

	public abstract int getThreadsCount();
}
