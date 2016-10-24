package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.rules.entity.AlertRule;

public interface Handler<T extends Metrics> {
   public void handle(T metrics);

   public void initialize(AlertRule rule);
}
