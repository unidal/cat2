package org.unidal.cat.core.alert.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.CatAlertModule;
import org.unidal.cat.core.alert.config.DefaultAlertConfiguration;
import org.unidal.cat.core.alert.metric.DefaultMetricsDispatcher;
import org.unidal.cat.core.alert.metric.DefaultMetricsEngine;
import org.unidal.cat.core.alert.metric.DefaultMetricsManager;
import org.unidal.cat.core.alert.metric.DefaultMetricsQueue;
import org.unidal.cat.core.alert.rule.DefaultAlertRuleService;
import org.unidal.cat.core.alert.service.DefaultAlertReportService;
import org.unidal.cat.core.alert.service.LocalAlertReportBuilder;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(CatAlertModule.class));

      all.add(A(DefaultAlertConfiguration.class));
      all.add(A(LocalAlertReportBuilder.class));
      all.add(A(DefaultAlertReportService.class));
      all.add(A(DefaultAlertRuleService.class));

      all.add(A(DefaultMetricsManager.class));
      all.add(A(DefaultMetricsEngine.class));
      all.add(A(DefaultMetricsDispatcher.class));
      all.add(A(DefaultMetricsQueue.class));

      // Please keep it as last
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
