package org.unidal.cat.plugin.events.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.events.EventsConfigService;
import org.unidal.cat.plugin.events.EventsPipeline;
import org.unidal.cat.plugin.events.filter.EventsHelper;
import org.unidal.cat.plugin.events.filter.EventsNameFilter;
import org.unidal.cat.plugin.events.filter.EventsNameGraphFilter;
import org.unidal.cat.plugin.events.filter.EventsTypeFilter;
import org.unidal.cat.plugin.events.filter.EventsTypeGraphFilter;
import org.unidal.cat.plugin.events.model.EventsReportAggregator;
import org.unidal.cat.plugin.events.model.EventsReportDelegate;
import org.unidal.cat.plugin.events.model.EventsReportManager;
import org.unidal.cat.plugin.events.reducer.EventsDailyReducer;
import org.unidal.cat.plugin.events.reducer.EventsMonthlyReducer;
import org.unidal.cat.plugin.events.reducer.EventsWeeklyReducer;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(EventsPipeline.class));
      all.add(A(EventsConfigService.class));
      
      all.add(A(EventsReportManager.class));
      all.add(A(EventsReportDelegate.class));
      all.add(A(EventsReportAggregator.class));
      all.add(A(EventsHelper.class));

      all.add(A(EventsTypeFilter.class));
      all.add(A(EventsNameFilter.class));
      all.add(A(EventsTypeGraphFilter.class));
      all.add(A(EventsNameGraphFilter.class));

      all.add(A(EventsDailyReducer.class));
      all.add(A(EventsWeeklyReducer.class));
      all.add(A(EventsMonthlyReducer.class));

      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }
}
