package org.unidal.cat.core.alert.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

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

import junit.framework.Assert;

import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.lifecycle.LifecycleHandler;
import org.codehaus.plexus.lifecycle.UndefinedLifecycleHandlerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.GzipFilter;
import org.unidal.cat.core.alert.config.AlertConfiguration;
import org.unidal.cat.core.alert.config.DefaultAlertConfiguration;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.DefaultNativeBuilder;
import org.unidal.cat.core.alert.rule.RuleService;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
import org.unidal.lookup.ContainerLoader;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.extension.PostConstructionPhase;
import org.unidal.test.jetty.JettyServer;

public class AlertReportServiceTest extends JettyServer {
   @After
   public void after() throws Exception {
      super.stopServer();
   }

   @Before
   public void before() throws Exception {
      System.setProperty("devMode", "true");
      super.startServer();
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
      return 2537;
   }

   @Override
   protected boolean isWebXmlDefined() {
      return false;
   }

   @Override
   protected void postConfigure(WebAppContext context) {
      context.addServlet(new ServletHolder(new MockServlet()), "/alert/service/*");
      context.addFilter(MockGzipFilter.class, "/*", Handler.ALL);
   }

   @Override
   protected void setupContainer() throws Exception {
      PlexusContainer container = ContainerLoader.getDefaultContainer(getConfiguration());
      DefaultContext context = new DefaultContext();

      context.put("plexus", container);
      contextualize(context);
   }

   @Test
   public void test() {
      AlertReportService service = lookup(AlertReportService.class);
      AlertReport report = service.getReport();
      AlertMachine firstMachine = report.getMachines().values().iterator().next();

      Assert.assertEquals("[mock1, mock2]", firstMachine.getEvents().keySet().toString());

      AlertEvent firstEvent = firstMachine.getEvents().values().iterator().next();

      Assert.assertEquals("type0", firstEvent.getMetrics().get(0).get("type"));
   }

   @Named(type = AlertConfiguration.class)
   public static final class MockAlertConfiguration extends DefaultAlertConfiguration {
      @Override
      public int getRemoteCallReadTimeoutInMillis() {
         return super.getRemoteCallReadTimeoutInMillis() * 100;
      }

      @Override
      public String getServerUri(String server) {
         return "http://localhost:2537/cat/alert/service?op=binary";
      }
   }

   @Named(type = MetricsBuilder.class, value = "mock1")
   public static class MockAlertMetricBuilder1 implements MetricsBuilder {
      @Override
      public void build(AlertEvent event) {
         event.setTypeName("mock1");

         for (int i = 0; i < 3; i++) {
            event.addMetric(new AlertMetric().set("type", "type" + i).set("name", "name" + i));
         }
      }

      @Override
      public Class<? extends Metrics> getMetricsType() {
         throw new UnsupportedOperationException();
      }
   }

   @Named(type = MetricsBuilder.class, value = "mock2")
   public static class MockAlertMetricBuilder2 implements MetricsBuilder {
      @Override
      public void build(AlertEvent event) {
         event.setTypeName("mock2");

         for (int i = 0; i < 3; i++) {
            event.addMetric(new AlertMetric().set("type", "type" + i).set("name", "name" + i));
         }
      }

      @Override
      public Class<? extends Metrics> getMetricsType() {
         throw new UnsupportedOperationException();
      }
   }

   @Named(type = RuleService.class)
   public static class MockAlertRuleService implements RuleService {
      @Override
      public Set<String> getAttributes(String type, String name) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSetDef> getRuleSets() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Set<String> getTypes() {
         return setOf("mock1", "mock2");
      }

      private Set<String> setOf(String... values) {
         Set<String> set = new LinkedHashSet<String>();

         for (String value : values) {
            set.add(value);
         }

         return set;
      }
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

   private class MockServlet extends HttpServlet {
      private static final long serialVersionUID = 1L;

      private AlertReportBuilder m_builder;

      @Override
      public void init(ServletConfig config) throws ServletException {
         super.init(config);

         PlexusContainer container = AlertReportServiceTest.this.getContainer();

         try {
            m_builder = container.lookup(AlertReportBuilder.class);
         } catch (ComponentLookupException e) {
            throw new ServletException(String.format("Error when init MockServlet."), e);
         }
      }

      @Override
      protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
         try {
            AlertReport report = m_builder.build();
            String acceptEncoding = req.getHeader("Accept-Encoding");

            // res.setContentType("application/octet-stream");

            if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
               res.setHeader("Content-Encoding", "gzip");

               GZIPOutputStream out = new GZIPOutputStream(res.getOutputStream());

               DefaultNativeBuilder.build(report, out);
               out.finish();
               out.flush();
            } else {
               OutputStream out = res.getOutputStream();

               DefaultNativeBuilder.build(report, out);
               out.flush();
            }
         } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
         }
      }
   }
}
