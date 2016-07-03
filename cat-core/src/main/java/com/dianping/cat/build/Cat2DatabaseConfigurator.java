package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class Cat2DatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      defineSimpleTableProviderComponents(all, "cat", org.unidal.cat.dal.report._INDEX.getEntityClasses());
      defineDaoComponents(all, org.unidal.cat.dal.report._INDEX.getDaoClasses());

      return all;
   }
}
