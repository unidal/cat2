package org.unidal.cat.core.report.remote;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.report.remote.RemoteReportTest.MockReportConfiguration;
import org.unidal.cat.core.report.remote.RemoteReportTest.MockReportDelegate;
import org.unidal.cat.core.report.remote.RemoteReportTest.MockReportFilter;
import org.unidal.cat.core.report.remote.RemoteReportTest.MockReportManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class RemoteReportTestConfigurator extends AbstractResourceConfigurator {
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
		return RemoteReportTest.class;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new RemoteReportTestConfigurator());
	}
}
