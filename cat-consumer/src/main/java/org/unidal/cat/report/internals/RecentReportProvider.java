package org.unidal.cat.report.internals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportConfiguration;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.cat.report.spi.remote.RemoteStub;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportProvider.class, value = RecentReportProvider.ID)
public class RecentReportProvider<T extends Report> implements ReportProvider<T>, Initializable {
	public static final String ID = "recent";

	@Inject
	private RemoteStub m_stub;

	@Inject
	private ReportConfiguration m_configuration;

	private ExecutorService m_pool;

	@Override
	public boolean isEligible(RemoteContext ctx, ReportDelegate<T> delegate) {
		return !ctx.getPeriod().isHistorical(ctx.getStartTime());
	}

	@Override
	public T getReport(final RemoteContext ctx, final ReportDelegate<T> delegate) throws IOException {
		Map<String, Boolean> servers = m_configuration.getServers();
		int len = servers.size();
		List<Callable<T>> callables = new ArrayList<Callable<T>>(servers.size());
		// final RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), domain, startTime, period, filter);

		for (Map.Entry<String, Boolean> e : servers.entrySet()) {
			if (e.getValue().booleanValue()) {
				final String server = e.getKey();

				callables.add(new Callable<T>() {
					@Override
					public T call() throws Exception {
						InputStream in = m_stub.getReport(ctx, server);
						T report = delegate.readStream(in);

						return report;
					}
				});
			}
		}

		List<T> reports = new ArrayList<T>(len);
		int timeout = m_configuration.getRemoteCallTimeoutInMillis();

		try {
			List<Future<T>> futures = m_pool.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);

			for (Future<T> future : futures) {
				if (future.isDone()) {
					try {
						reports.add(future.get());
					} catch (ExecutionException e) {
						// TODO mark as failure
						Cat.logError(e.getCause());
					}
				} else {
					// TODO mark as failure
				}
			}
		} catch (InterruptedException e) {
			Cat.logError(e);
		}

		if (reports.isEmpty()) {
			return null;
		} else {
			T report = delegate.aggregate(ctx.getPeriod(), reports);
			ReportFilter<Report> filter = ctx.getFilter();

			if (filter != null) {
				filter.applyTo(ctx, report);
			}

			return report;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		int threads = m_configuration.getRemoteCallThreads();

		m_pool = Threads.forPool().getFixedThreadPool("cat", threads);
	}
}
