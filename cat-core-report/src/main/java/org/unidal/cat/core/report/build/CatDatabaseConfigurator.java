package org.unidal.cat.core.report.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();


      defineSimpleTableProviderComponents(all, "cat", org.unidal.cat.core.report.dal._INDEX.getEntityClasses());
      defineDaoComponents(all, org.unidal.cat.core.report.dal._INDEX.getDaoClasses());

      return all;
   }
}
