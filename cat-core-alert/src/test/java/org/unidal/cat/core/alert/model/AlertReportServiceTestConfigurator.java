package org.unidal.cat.core.alert.model;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.model.AlertReportServiceTest.MockAlertConfiguration;
import org.unidal.cat.core.alert.model.AlertReportServiceTest.MockAlertMetricBuilder1;
import org.unidal.cat.core.alert.model.AlertReportServiceTest.MockAlertMetricBuilder2;
import org.unidal.cat.core.alert.model.AlertReportServiceTest.MockAlertRuleService;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class AlertReportServiceTestConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(MockAlertConfiguration.class));
      all.add(A(MockAlertMetricBuilder1.class));
      all.add(A(MockAlertMetricBuilder2.class));
      all.add(A(MockAlertRuleService.class));

      return all;
   }

   @Override
   protected Class<?> getTestClass() {
      return AlertReportServiceTest.class;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new AlertReportServiceTestConfigurator());
   }
}
