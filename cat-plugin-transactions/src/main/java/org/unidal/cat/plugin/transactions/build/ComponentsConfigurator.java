package org.unidal.cat.plugin.transactions.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.transactions.TransactionsPipeline;
import org.unidal.cat.plugin.transactions.config.TransactionsConfigService;
import org.unidal.cat.plugin.transactions.filter.TransactionsHelper;
import org.unidal.cat.plugin.transactions.filter.TransactionsNameFilter;
import org.unidal.cat.plugin.transactions.filter.TransactionsNameGraphFilter;
import org.unidal.cat.plugin.transactions.filter.TransactionsTypeFilter;
import org.unidal.cat.plugin.transactions.filter.TransactionsTypeGraphFilter;
import org.unidal.cat.plugin.transactions.model.TransactionsReportAggregator;
import org.unidal.cat.plugin.transactions.model.TransactionsReportDelegate;
import org.unidal.cat.plugin.transactions.model.TransactionsReportManager;
import org.unidal.cat.plugin.transactions.reducer.TransactionsDailyReducer;
import org.unidal.cat.plugin.transactions.reducer.TransactionsMonthlyReducer;
import org.unidal.cat.plugin.transactions.reducer.TransactionsWeeklyReducer;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(TransactionsPipeline.class));
      all.add(A(TransactionsConfigService.class));
      
      all.add(A(TransactionsReportManager.class));
      all.add(A(TransactionsReportDelegate.class));
      all.add(A(TransactionsReportAggregator.class));
      all.add(A(TransactionsHelper.class));

      all.add(A(TransactionsTypeFilter.class));
      all.add(A(TransactionsNameFilter.class));
      all.add(A(TransactionsTypeGraphFilter.class));
      all.add(A(TransactionsNameGraphFilter.class));

      all.add(A(TransactionsDailyReducer.class));
      all.add(A(TransactionsWeeklyReducer.class));
      all.add(A(TransactionsMonthlyReducer.class));

      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }
}
