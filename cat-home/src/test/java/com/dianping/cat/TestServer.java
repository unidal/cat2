package com.dianping.cat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.GzipFilter;
import org.unidal.test.jetty.JettyServer;

import java.io.File;

@RunWith(JUnit4.class)
public class TestServer extends JettyServer {
	public static void main(String[] args) throws Exception {
        // Added by yj.huang
        System.setProperty("user.dir", System.getProperty("user.dir") + "\\cat-home");

		TestServer server = new TestServer();
		System.setProperty("devMode", "true");
		server.startServer();
		server.startWebApp();
		server.stopServer();
	}
	
	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

    @Override
    protected File getWarRoot() {
        // yj.huang Need call getAbsoluteFile() to make sure the file exists
        return new File("src/main/webapp").getAbsoluteFile();
    }

	@Override
	protected String getContextPath() {
		return "/cat";
	}

	@Override
	protected int getServerPort() {
		return 2281;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addFilter(GzipFilter.class, "/*", Handler.ALL);
	}

	@Test
	public void startWebApp() throws Exception {
		// open the page in the default browser
		display("/cat/r");
		waitForAnyKey();
	}
}
