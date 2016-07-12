package com.dianping.cat.consumer.build;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      defineSimpleTableProviderComponents(all, "cat", com.dianping.cat.consumer.dal._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.cat.consumer.dal._INDEX.getDaoClasses());

      return all;
   }
}
