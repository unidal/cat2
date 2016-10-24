package org.unidal.cat.core.alert.rule;

import java.util.List;
import java.util.Set;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSet;

public interface AlertRuleService {
   public Set<String> getAttributes(String type, String name);

   public List<AlertRuleSet> getRuleSetByAttribute(String type, String name, String value);

   public List<AlertRuleSet> getRuleSets();

   public Set<String> getTypes();
}
