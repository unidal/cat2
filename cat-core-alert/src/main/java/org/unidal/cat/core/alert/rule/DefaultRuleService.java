package org.unidal.cat.core.alert.rule;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSetDef;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleService.class)
public class DefaultRuleService implements RuleService {
   @Override
   public Set<String> getAttributes(String type, String name) {
      return Collections.emptySet();
   }

   @Override
   public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
      return Collections.emptyList();
   }

   @Override
   public Set<String> getTypes() {
      return Collections.emptySet();
   }

   @Override
   public List<AlertRuleSetDef> getRuleSets() {
      return Collections.emptyList();
   }
}
