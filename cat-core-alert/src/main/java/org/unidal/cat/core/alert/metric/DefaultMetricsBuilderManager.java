package org.unidal.cat.core.alert.metric;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = MetricsBuilderManager.class)
public class DefaultMetricsBuilderManager extends ContainerHolder implements MetricsBuilderManager, Initializable {
   private Map<String, MetricsBuilder> m_builders = new LinkedHashMap<String, MetricsBuilder>();

   @Override
   public MetricsBuilder getBuilder(String type) {
      return m_builders.get(type);
   }

   @Override
   public void initialize() throws InitializationException {
      Map<String, MetricsBuilder> handlers = lookupMap(MetricsBuilder.class);

      for (Map.Entry<String, MetricsBuilder> e : handlers.entrySet()) {
         m_builders.put(e.getKey(), e.getValue());
      }
   }

   @Override
   public Map<String, MetricsBuilder> getBuilders() {
      return m_builders;
   }
}
