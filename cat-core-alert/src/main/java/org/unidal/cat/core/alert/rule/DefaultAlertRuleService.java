package org.unidal.cat.core.alert.rule;

import java.util.List;
import java.util.Set;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSet;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertRuleService.class)
public class DefaultAlertRuleService implements AlertRuleService {
   @Override
   public Set<String> getAttributes(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<AlertRuleSet> getRuleSetByAttribute(String type, String name, String value) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<String> getTypes() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<AlertRuleSet> getRuleSets() {
      return null;
   }
}
