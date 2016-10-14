package org.unidal.cat.core.alert.metric;

import java.util.Map;

public interface MetricsManager {
   public MetricsBuilder getBuilder(String type);

   public Map<String, MetricsBuilder> getBuilders();
}
