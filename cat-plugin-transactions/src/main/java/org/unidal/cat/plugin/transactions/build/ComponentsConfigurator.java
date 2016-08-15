package org.unidal.cat.plugin.transactions.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.plugin.transactions.TransactionsHelper;
import org.unidal.cat.plugin.transactions.TransactionsPipeline;
import org.unidal.cat.plugin.transactions.TransactionsReportDelegate;
import org.unidal.cat.plugin.transactions.TransactionsReportManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(TransactionsPipeline.class));
      all.add(A(TransactionsReportManager.class));
      all.add(A(TransactionsReportDelegate.class));
      all.add(A(TransactionsHelper.class));

      return all;
   }
}
