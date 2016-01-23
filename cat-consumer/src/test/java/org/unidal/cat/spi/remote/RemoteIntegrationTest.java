package org.unidal.cat.spi.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.lifecycle.LifecycleHandler;
import org.codehaus.plexus.lifecycle.UndefinedLifecycleHandlerException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.GzipFilter;
import org.unidal.cat.spi.DefaultReportConfiguration;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportConfiguration;
import org.unidal.cat.spi.ReportFilterManager;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.lookup.ContainerLoader;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.extension.PostConstructionPhase;
import org.unidal.test.jetty.JettyServer;

/**
 * <ul>
 * Following features are tested:
 * <li>RemoteStub and RemoteSkeleton</li>
 * <li>with ReportFilter or not</li>
 * <li>with GZip if with ReportFilter</li>
 * </ul>
 */
@RunWith(JUnit4.class)
public class RemoteIntegrationTest extends JettyServer {
	@After
	public void after() throws Exception {
		super.stopServer();
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

	private RemoteContext buildContext(ReportFilter<?> filter) {
		ReportPeriod period = ReportPeriod.HOUR;
		Date startTime = period.getStartTime(new Date());

		return new DefaultRemoteContext("mock", "domain", startTime, period, filter);
	}

	private void check(RemoteStub stub, ReportFilter<?> filter, String expected, String... keyValuePairs)
	      throws IOException {
		RemoteContext ctx = buildContext(filter);

		for (int i = 0; i < keyValuePairs.length; i += 2) {
			String key = keyValuePairs[i];
			String value = keyValuePairs[i + 1];

			ctx.setProperty(key, value);
		}

		InputStream in = stub.getReport(ctx, "localhost");
		String actual = Files.forIO().readFrom(in, "utf-8");

		if ("true".equals(ctx.getProperty("gzip", null))) {
			Assert.assertEquals("GZIPInputStream", in.getClass().getSimpleName());
		} else {
			Assert.assertEquals("HttpInputStream", in.getClass().getSimpleName());
		}

		Assert.assertEquals(expected, actual);
	}

	private DefaultContainerConfiguration getConfiguration() throws Exception, UndefinedLifecycleHandlerException {
		String defaultConfigurationName = getClass().getName().replace('.', '/') + ".xml";
		DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();

		configuration.setName("test").setContext(Collections.emptyMap());
		configuration.setContainerConfiguration(defaultConfigurationName);

		LifecycleHandler plexus = configuration.getLifecycleHandlerManager().getLifecycleHandler(
		      PlexusConstants.PLEXUS_KEY);

		plexus.addBeginSegment(new PostConstructionPhase());

		return configuration;
	}

	@Override
	protected String getContextPath() {
		return "/cat";
	}

	@Override
	protected int getServerPort() {
		return 7366;
	}

	@Override
	protected boolean isWebXmlDefined() {
		return false;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addServlet(new ServletHolder(new MockServlet()), "/r/service/*");
		context.addFilter(MockGzipFilter.class, "/*", Handler.ALL);
	}

	@Override
	protected void setupContainer() throws Exception {
		PlexusContainer container = ContainerLoader.getDefaultContainer(getConfiguration());
		DefaultContext context = new DefaultContext();

		context.put("plexus", container);
		contextualize(context);
	}

	@Test(expected = IOException.class)
	public void testWithBadFilter() throws Exception {
		RemoteStub stub = lookup(RemoteStub.class);
		ReportFilter<?> filter = lookup(ReportFilter.class, "mock:mock");

		// make sure component configuration work
		Assert.assertEquals(MockReportConfiguration.class, lookup(ReportConfiguration.class).getClass());

		check(stub, filter, null, "a", "1", "b", "2", "error", "true");
	}

	@Test
	public void testWithFilter() throws Exception {
		RemoteStub stub = lookup(RemoteStub.class);
		ReportFilter<?> filter = lookup(ReportFilter.class, "mock:mock");

		// make sure component configuration work
		Assert.assertEquals(MockReportConfiguration.class, lookup(ReportConfiguration.class).getClass());

		check(stub, filter, "MockReport[domain, hour, {a=1, b=2}]", "a", "1", "b", "2");
	}

	@Test
	public void testWithGzip() throws Exception {
		RemoteStub stub = lookup(RemoteStub.class);
		ReportFilter<?> filter = lookup(ReportFilter.class, "mock:mock");

		// make sure component configuration work
		Assert.assertEquals(MockReportConfiguration.class, lookup(ReportConfiguration.class).getClass());

		check(stub, filter, "MockReport[domain, hour, {a=1, b=2, gzip=true}]", "a", "1", "b", "2", "gzip", "true");
	}

	@Test
	public void testWithoutFilter() throws Exception {
		RemoteStub stub = lookup(RemoteStub.class);

		// make sure component configuration work
		Assert.assertEquals(MockReportConfiguration.class, lookup(ReportConfiguration.class).getClass());

		check(stub, null, "MockReport[domain, hour, null]", "a", "1", "b", "2");
	}

	public static class MockGzipFilter implements Filter {
		private GzipFilter m_gzipFilter = new GzipFilter();

		@Override
		public void destroy() {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
		      ServletException {
			HttpServletRequest req = (HttpServletRequest) request;

			if ("true".equals(req.getParameter("gzip"))) {
				m_gzipFilter.doFilter(request, response, chain);
			} else {
				chain.doFilter(request, response);
			}
		}

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			m_gzipFilter.init(filterConfig);
		}
	}

	static class MockReport implements Report {
		private String m_domain;

		private Date m_startTime;

		private Date m_endTime;

		private ReportPeriod m_period;

		private String m_notes;

		public MockReport(ReportPeriod period, Date startTime, String domain) {
			m_period = period;
			m_startTime = startTime;
			m_domain = domain;
		}

		@Override
		public String getDomain() {
			return m_domain;
		}

		@Override
		public Date getEndTime() {
			return m_endTime;
		}

		public String getNotes() {
			return m_notes;
		}

		@Override
		public ReportPeriod getPeriod() {
			return m_period;
		}

		@Override
		public Date getStartTime() {
			return m_startTime;
		}

		public void setNotes(String notes) {
			m_notes = notes;
		}

		@Override
		public String toString() {
			return String.format("%s[%s, %s, %s]", getClass().getSimpleName(), m_domain, m_period.getName(), m_notes);
		}

		public void writeTo(OutputStream out) throws IOException {
			out.write(toString().getBytes());
		}
	}

	@Named(type = ReportConfiguration.class)
	public static final class MockReportConfiguration extends DefaultReportConfiguration {
		@Override
		public int getRemoteCallReadTimeoutInMillis() {
			return super.getRemoteCallReadTimeoutInMillis() * 100;
		}

		@Override
		public String getServerUriPrefix(String server) {
			return "http://localhost:7366/cat/r/service";
		}
	}

	@Named(type = ReportDelegate.class, value = "mock")
	public static final class MockReportDelegate implements ReportDelegate<MockReport> {
		@Override
		public MockReport aggregate(ReportPeriod period, Collection<MockReport> reports) {
			if (reports.size() == 1) {
				return reports.iterator().next();
			} else {
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public String buildXml(MockReport report) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MockReport createLocal(ReportPeriod period, String domain, Date startTime) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return "mock";
		}

		@Override
		public MockReport parseXml(String xml) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MockReport readStream(InputStream in) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeStream(OutputStream out, MockReport report) throws IOException {
			report.writeTo(out);
		}
	}

	@Named(type = ReportFilter.class, value = "mock:mock")
	public static final class MockReportFilter implements ReportFilter<MockReport> {
		@Override
		public String getId() {
			return "mock";
		}

		@Override
		public String getReportName() {
			return "mock";
		}

		@Override
		public MockReport screen(RemoteContext ctx, MockReport report) {
			return report;
		}

		@Override
		public void tailor(RemoteContext ctx, MockReport report) {
			if ("true".equals(ctx.getProperty("error", null))) {
				throw new RuntimeException("Unknown issue.");
			} else {
				report.setNotes(ctx.getProperties().toString());
			}
		}
	}

	@Named(type = ReportManager.class, value = "mock")
	public static final class MockReportManager implements ReportManager<MockReport> {
		@Override
		public void doCheckpoint(Date date, int index, boolean atEnd) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void doInitLoad(Date date, int index) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public MockReport getLocalReport(String domain, Date startTime, int index, boolean createIfNotExist) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<MockReport> getLocalReports(ReportPeriod period, Date startTime, String domain) throws IOException {
			return Arrays.asList(new MockReport(period, startTime, domain));
		}

		@Override
		public MockReport getReport(ReportPeriod period, Date startTime, String domain, String filterId,
		      String... keyValuePairs) throws IOException {
			throw new UnsupportedOperationException();
		}
	}

	class MockServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		private ReportFilterManager m_manager;

		private RemoteSkeleton m_skeleton;

		@SuppressWarnings("unchecked")
		private RemoteContext createContext(HttpServletRequest req) {
			String path = req.getPathInfo();
			List<String> parts = Splitters.by('/').trim().split(path.substring(1));
			String name = parts.size() > 0 ? parts.get(0) : null;
			String domain = parts.size() > 1 ? parts.get(1) : null;
			String period = parts.size() > 2 ? parts.get(2) : null;
			String startTime = parts.size() > 3 ? parts.get(3) : null;
			String filterId = parts.size() > 4 ? parts.get(4) : null;

			ReportPeriod p = ReportPeriod.getByName(period, null);
			ReportFilter<Report> filter = m_manager.getFilter(name, filterId);
			DefaultRemoteContext ctx = new DefaultRemoteContext(name, domain, p.parse(startTime, null), p, filter);

			List<String> keys = Collections.list(req.getParameterNames());

			for (String key : keys) {
				String value = req.getParameter(key);

				ctx.setProperty(key, value);
			}

			return ctx;
		}

		@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);

			PlexusContainer container = RemoteIntegrationTest.this.getContainer();

			try {
				m_skeleton = container.lookup(RemoteSkeleton.class);
				m_manager = container.lookup(ReportFilterManager.class);
			} catch (ComponentLookupException e) {
				throw new ServletException(String.format("Error when init MockServlet."), e);
			}
		}

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			OutputStream out = res.getOutputStream();
			RemoteContext ctx = createContext(req);

			try {
				m_skeleton.handleReport(ctx, out);
			} catch (IOException e) {
				// e.printStackTrace();
				throw e;
			} catch (RuntimeException e) {
				// e.printStackTrace();
				throw e;
			}
		}
	}
}
