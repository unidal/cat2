package org.unidal.cat.core.alert.rule;

import java.util.List;
import java.util.Set;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSetDef;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleService.class)
public class DefaultRuleService implements RuleService {
   @Override
   public Set<String> getAttributes(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<String> getTypes() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<AlertRuleSetDef> getRuleSets() {
      return null;
   }
}
