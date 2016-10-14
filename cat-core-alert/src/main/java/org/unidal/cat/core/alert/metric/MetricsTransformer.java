package org.unidal.cat.core.alert.metric;

import org.unidal.cat.core.alert.model.entity.AlertMetric;

public interface MetricsTransformer<T extends Metrics> {
   public AlertMetric transform(T metrics);

   public T transform(AlertMetric metric);
}
