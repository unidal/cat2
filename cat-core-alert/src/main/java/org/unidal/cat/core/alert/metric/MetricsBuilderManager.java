package org.unidal.cat.core.alert.metric;

import java.util.Map;

public interface MetricsBuilderManager {
   public MetricsBuilder getBuilder(String type);

   public Map<String, MetricsBuilder> getBuilders();
}
