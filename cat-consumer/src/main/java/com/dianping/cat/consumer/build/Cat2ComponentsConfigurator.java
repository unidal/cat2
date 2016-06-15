package com.dianping.cat.consumer.build;

import com.dianping.cat.service.ProjectService;
import org.unidal.cat.config.internal.DBConfigManager;
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
import org.unidal.cat.plugin.problem.DefaultAbstractProblemHandler;
import org.unidal.cat.plugin.problem.LongExecutionAbstractProblemHandler;
import org.unidal.cat.plugin.problem.ProblemPipeline;
import org.unidal.cat.plugin.problem.ProblemReportAggregator;
import org.unidal.cat.plugin.problem.ProblemReportAnalyzer;
import org.unidal.cat.plugin.problem.ProblemReportDelegate;
import org.unidal.cat.plugin.problem.ProblemReportManager;
import org.unidal.cat.plugin.problem.filter.ProblemDetailFilter;
import org.unidal.cat.plugin.problem.filter.ProblemGraphFilter;
import org.unidal.cat.plugin.problem.filter.ProblemHomePageFilter;
import org.unidal.cat.plugin.problem.filter.ProblemReportHelper;
import org.unidal.cat.plugin.problem.filter.ProblemThreadFilter;
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
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(TransactionPipeline.class));
      all.add(A(TransactionConfigProvider.class));
      all.add(A(EventPipeline.class));
      all.add(A(ProblemPipeline.class));
      all.add(A(DBConfigManager.class));
      all.add(A(ProjectService.class));

      all.addAll(defineTransactionComponents());
      all.addAll(defineEventComponents());
      all.addAll(defineProblemComponents());

      return all;
   }

   private List<Component> defineEventComponents() {
      final List<Component> all = new ArrayList<Component>();

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

   private Collection<Component> defineProblemComponents() {
      final List<Component> all = new ArrayList<Component>();

      all.add(A(ProblemReportManager.class));
      all.add(A(ProblemReportAggregator.class));
      all.add(A(ProblemReportDelegate.class));
      all.add(A(ProblemReportAnalyzer.class));

      all.add(A(DefaultAbstractProblemHandler.class));
      all.add(A(LongExecutionAbstractProblemHandler.class));

      all.add(A(ProblemReportHelper.class));
      all.add(A(ProblemHomePageFilter.class));
      all.add(A(ProblemGraphFilter.class));
      all.add(A(ProblemThreadFilter.class));
      all.add(A(ProblemDetailFilter.class));
      return all;
   }

   private List<Component> defineTransactionComponents() {
      final List<Component> all = new ArrayList<Component>();

      all.add(A(TransactionReportManager.class));
      all.add(A(TransactionReportAggregator.class));
      all.add(A(TransactionReportDelegate.class));
      all.add(A(TransactionReportAnalyzer.class));

      all.add(A(TransactionReportHelper.class));
      all.add(A(TransactionTypeFilter.class));
      all.add(A(TransactionTypeGraphFilter.class));
      all.add(A(TransactionNameFilter.class));
      all.add(A(TransactionNameGraphFilter.class));
      all.add(A(TransactionAllTypeFilter.class));
      all.add(A(TransactionAllTypeGraphFilter.class));
      all.add(A(TransactionAllNameFilter.class));
      all.add(A(TransactionAllNameGraphFilter.class));

      return all;
   }
}
