package org.unidal.cat.core.config.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.config.CatConfigModule;
import org.unidal.cat.core.config.DefaultDomainGroupConfigService;
import org.unidal.cat.core.config.DefaultDomainOrgConfigService;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(CatConfigModule.class));

      all.add(A(DefaultDomainGroupConfigService.class));
      all.add(A(DefaultDomainOrgConfigService.class));

      return all;
   }
}
