package org.unidal.cat.plugin.transaction.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConfigProvider;
import org.unidal.cat.plugin.transaction.TransactionPipeline;
import org.unidal.cat.plugin.transaction.TransactionReportAggregator;
import org.unidal.cat.plugin.transaction.TransactionReportAnalyzer;
import org.unidal.cat.plugin.transaction.TransactionReportDelegate;
import org.unidal.cat.plugin.transaction.TransactionReportManager;
import org.unidal.cat.plugin.transaction.filter.TransactionAllNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllTypeGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionReportHelper;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeGraphFilter;
import org.unidal.cat.plugin.transaction.reducer.TransactionDailyReducer;
import org.unidal.cat.plugin.transaction.reducer.TransactionMonthlyReducer;
import org.unidal.cat.plugin.transaction.reducer.TransactionWeeklyReducer;
import org.unidal.cat.plugin.transaction.view.svg.DefaultGraphBuilder;
import org.unidal.cat.plugin.transaction.view.svg.DefaultValueTranslater;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(TransactionPipeline.class));
		all.add(A(TransactionConfigProvider.class));

		all.add(A(TransactionReportManager.class));
		all.add(A(TransactionReportAggregator.class));
		all.add(A(TransactionReportDelegate.class));
		all.add(A(TransactionReportAnalyzer.class));

		// filter
		all.add(A(TransactionReportHelper.class));
		all.add(A(TransactionTypeFilter.class));
		all.add(A(TransactionTypeGraphFilter.class));
		all.add(A(TransactionNameFilter.class));
		all.add(A(TransactionNameGraphFilter.class));
		all.add(A(TransactionAllTypeFilter.class));
		all.add(A(TransactionAllTypeGraphFilter.class));
		all.add(A(TransactionAllNameFilter.class));
		all.add(A(TransactionAllNameGraphFilter.class));

		// reducer
		all.add(A(TransactionDailyReducer.class));
		all.add(A(TransactionWeeklyReducer.class));
		all.add(A(TransactionMonthlyReducer.class));

		all.add(A(DefaultGraphBuilder.class));
		all.add(A(DefaultValueTranslater.class));

		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}
}
