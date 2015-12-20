package org.unidal.cat.report.spi.remote;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.report.spi.remote.RemoteIntegrationTest.MockReportConfiguration;
import org.unidal.cat.report.spi.remote.RemoteIntegrationTest.MockReportDelegate;
import org.unidal.cat.report.spi.remote.RemoteIntegrationTest.MockReportFilter;
import org.unidal.cat.report.spi.remote.RemoteIntegrationTest.MockReportManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class RemoteIntegrationTestConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(MockReportConfiguration.class));
		all.add(A(MockReportManager.class));
		all.add(A(MockReportDelegate.class));
		all.add(A(MockReportFilter.class));

		return all;
	}

	@Override
	protected Class<?> getTestClass() {
		return RemoteIntegrationTest.class;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new RemoteIntegrationTestConfigurator());
	}
}
