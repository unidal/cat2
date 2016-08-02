package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.event.page.transform.AllReportDistributionBuilder;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(AllReportDistributionBuilder.class));

		return all;
	}
}
