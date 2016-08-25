package org.unidal.cat.plugin.event.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.event.EventConfigProvider;
import org.unidal.cat.plugin.event.EventPipeline;
import org.unidal.cat.plugin.event.EventReportAnalyzer;
import org.unidal.cat.plugin.event.filter.EventHelper;
import org.unidal.cat.plugin.event.filter.EventNameFilter;
import org.unidal.cat.plugin.event.filter.EventNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventTypeFilter;
import org.unidal.cat.plugin.event.filter.EventTypeGraphFilter;
import org.unidal.cat.plugin.event.model.EventReportAggregator;
import org.unidal.cat.plugin.event.model.EventReportDelegate;
import org.unidal.cat.plugin.event.model.EventReportManager;
import org.unidal.cat.plugin.event.reducer.EventDailyReducer;
import org.unidal.cat.plugin.event.reducer.EventMonthlyReducer;
import org.unidal.cat.plugin.event.reducer.EventWeeklyReducer;
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
		all.add(A(EventConfigProvider.class));

		all.add(A(EventReportManager.class));
		all.add(A(EventReportAggregator.class));
		all.add(A(EventReportDelegate.class));
		all.add(A(EventReportAnalyzer.class));

		// filter
		all.add(A(EventHelper.class));
		all.add(A(EventTypeFilter.class));
		all.add(A(EventTypeGraphFilter.class));
		all.add(A(EventNameFilter.class));
		all.add(A(EventNameGraphFilter.class));

		// reducer
		all.add(A(EventDailyReducer.class));
		all.add(A(EventWeeklyReducer.class));
		all.add(A(EventMonthlyReducer.class));

		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}
}
