package org.unidal.cat.core.alert.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.CatAlertModule;
import org.unidal.cat.core.alert.engine.DefaultAlertEngine;
import org.unidal.cat.core.alert.engine.DefaultAlertRegistry;
import org.unidal.cat.core.alert.internals.DefaultAlertConfiguration;
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
      all.add(A(DefaultAlertEngine.class));
      all.add(A(DefaultAlertRegistry.class));

      // Please keep it as last
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
