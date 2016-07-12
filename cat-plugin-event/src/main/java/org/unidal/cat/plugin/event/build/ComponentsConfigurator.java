package org.unidal.cat.plugin.event.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.event.EventPipeline;
import org.unidal.cat.plugin.event.EventReportAggregator;
import org.unidal.cat.plugin.event.EventReportAnalyzer;
import org.unidal.cat.plugin.event.EventReportDelegate;
import org.unidal.cat.plugin.event.EventReportManager;
import org.unidal.cat.plugin.event.filter.EventAllNameFilter;
import org.unidal.cat.plugin.event.filter.EventAllNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventAllTypeFilter;
import org.unidal.cat.plugin.event.filter.EventAllTypeGraphFilter;
import org.unidal.cat.plugin.event.filter.EventNameFilter;
import org.unidal.cat.plugin.event.filter.EventNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventReportHelper;
import org.unidal.cat.plugin.event.filter.EventTypeFilter;
import org.unidal.cat.plugin.event.filter.EventTypeGraphFilter;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(EventPipeline.class));

		all.add(A(EventReportManager.class));
		all.add(A(EventReportAggregator.class));
		all.add(A(EventReportDelegate.class));
		all.add(A(EventReportAnalyzer.class));

		all.add(A(EventReportHelper.class));
		all.add(A(EventTypeFilter.class));
		all.add(A(EventTypeGraphFilter.class));
		all.add(A(EventNameFilter.class));
		all.add(A(EventNameGraphFilter.class));
		all.add(A(EventAllTypeFilter.class));
		all.add(A(EventAllTypeGraphFilter.class));
		all.add(A(EventAllNameFilter.class));
		all.add(A(EventAllNameGraphFilter.class));

		return all;
	}
}
