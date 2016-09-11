package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.metric.DefaultBenchmarkManager;
import org.unidal.cat.service.internals.DefaultCompressionService;
import org.unidal.cat.service.internals.GzipCompressionService;
import org.unidal.cat.service.internals.ZlibCompressionService;
import org.unidal.cat.spi.command.DefaultCommandDispatcher;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.TcpSocketReceiver;

class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultBenchmarkManager.class));

      all.addAll(defineServiceComponents());
      all.addAll(defineTransportComponents());

      return all;
   }

   private List<Component> defineServiceComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultCompressionService.class));
      all.add(A(GzipCompressionService.class));
      all.add(A(ZlibCompressionService.class));

      return all;
   }

   private List<Component> defineTransportComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(TcpSocketReceiver.class)); // TODO remove it later

      all.add(A(DefaultCommandDispatcher.class));

      return all;
   }
}
