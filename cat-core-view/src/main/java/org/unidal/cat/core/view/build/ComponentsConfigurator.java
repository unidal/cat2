package org.unidal.cat.core.view.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.view.menu.DefaultMenuManager;
import org.unidal.cat.core.view.menu.DefaultMenuManagerManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultMenuManagerManager.class));
      all.add(A(DefaultMenuManager.class));

      return all;
   }
}
