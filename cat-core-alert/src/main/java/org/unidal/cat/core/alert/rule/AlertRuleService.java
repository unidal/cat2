package org.unidal.cat.core.alert.rule;

import java.util.List;
import java.util.Set;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSetDef;

public interface AlertRuleService {
   public Set<String> getAttributes(String type, String name);

   public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value);

   public List<AlertRuleSetDef> getRuleSets();

   public Set<String> getTypes();
}
