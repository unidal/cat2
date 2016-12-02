package org.unidal.cat.core.config.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.config.CatConfigModule;
import org.unidal.cat.core.config.SystemConfigStoreGroup;
import org.unidal.cat.core.config.service.DefaultContactorService;
import org.unidal.cat.core.config.service.DefaultDomainGroupConfigService;
import org.unidal.cat.core.config.service.DefaultDomainOrgConfigService;
import org.unidal.cat.core.config.spi.internals.DefaultConfigStoreManager;
import org.unidal.cat.core.config.view.ConfigMenuManager;
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
      all.add(A(SystemConfigStoreGroup.class));

      all.add(A(DefaultConfigStoreManager.class));

      all.add(A(ConfigMenuManager.class));

      all.add(A(DefaultDomainGroupConfigService.class));
      all.add(A(DefaultDomainOrgConfigService.class));
      all.add(A(DefaultContactorService.class));

      all.addAll(new CatDatabaseConfigurator().defineComponents());
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }
}
