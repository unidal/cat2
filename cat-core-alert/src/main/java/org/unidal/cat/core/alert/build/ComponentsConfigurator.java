package org.unidal.cat.core.alert.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.CatAlertModule;
import org.unidal.cat.core.alert.config.AlertConfigStoreGroup;
import org.unidal.cat.core.alert.config.DefaultAlertConfiguration;
import org.unidal.cat.core.alert.message.DefaultAlertMessageSink;
import org.unidal.cat.core.alert.message.DefaultAlertRecipientManager;
import org.unidal.cat.core.alert.message.DefaultAlertSenderManager;
import org.unidal.cat.core.alert.message.EmailAlertSender;
import org.unidal.cat.core.alert.metric.DefaultMetricsBuilderManager;
import org.unidal.cat.core.alert.metric.DefaultMetricsDispatcher;
import org.unidal.cat.core.alert.metric.DefaultMetricsEngine;
import org.unidal.cat.core.alert.metric.DefaultMetricsQueue;
import org.unidal.cat.core.alert.model.DefaultAlertReportService;
import org.unidal.cat.core.alert.model.LocalAlertReportBuilder;
import org.unidal.cat.core.alert.rule.DefaultRuleService;
import org.unidal.cat.core.alert.rule.RuleEvaluatorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(CatAlertModule.class));
      all.add(A(AlertConfigStoreGroup.class));

      all.add(A(DefaultAlertConfiguration.class));
      all.add(A(LocalAlertReportBuilder.class));
      all.add(A(DefaultAlertReportService.class));
      all.add(A(DefaultRuleService.class));
      all.add(A(RuleEvaluatorManager.class));

      all.add(A(DefaultAlertMessageSink.class));
      all.add(A(DefaultAlertRecipientManager.class));
      all.add(A(DefaultAlertSenderManager.class));
      all.add(A(EmailAlertSender.class));

      all.add(A(DefaultMetricsBuilderManager.class));
      all.add(A(DefaultMetricsEngine.class));
      all.add(A(DefaultMetricsDispatcher.class));
      all.add(A(DefaultMetricsQueue.class));

      all.addAll(new CatDatabaseConfigurator().defineComponents());

      // Please keep it as last
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
