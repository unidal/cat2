package org.unidal.cat.core.alert.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.model.AlertReportAggregator;
import org.unidal.cat.core.alert.model.AlertReportDelegate;
import org.unidal.cat.core.alert.model.AlertReportManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(AlertReportManager.class));
		all.add(A(AlertReportAggregator.class));
		all.add(A(AlertReportDelegate.class));
		
		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
