package org.unidal.cat.plugin.problem.build;

import org.unidal.cat.plugin.problem.DefaultProblemHandler;
import org.unidal.cat.plugin.problem.LongExecutionProblemHandler;
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
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(ProblemPipeline.class));

      all.add(A(ProblemReportManager.class));
      all.add(A(ProblemReportAggregator.class));
      all.add(A(ProblemReportDelegate.class));
      all.add(A(ProblemReportAnalyzer.class));

      all.add(A(DefaultProblemHandler.class));
      all.add(A(LongExecutionProblemHandler.class));

      all.add(A(ProblemReportHelper.class));
      all.add(A(ProblemHomePageFilter.class));
      all.add(A(ProblemGraphFilter.class));
      all.add(A(ProblemThreadFilter.class));
      all.add(A(ProblemDetailFilter.class));

      return all;
   }
}
