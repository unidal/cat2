package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.config.internals.DefaultClientConfigurationManager;
import org.unidal.cat.config.internals.DefaultClientEnvironmentSettings;
import org.unidal.cat.config.internals.DefaultServerDiscovery;
import org.unidal.cat.config.internals.LocalClientConfigurationProvider;
import org.unidal.cat.config.internals.RemoteClientConfigurationProvider;
import org.unidal.cat.internals.CatClientInitializer;
import org.unidal.cat.message.DefaultClientTransportHub;
import org.unidal.cat.message.MessageIdFactory;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.cat.message.command.DefaultCommandDispatcher;
import org.unidal.cat.message.internals.DefaultMessagePolicy;
import org.unidal.cat.transport.DefaultSocketAddressProvider;
import org.unidal.cat.transport.DefaultTransportConfiguration;
import org.unidal.cat.transport.TcpSocketStub;
import org.unidal.cat.transport.decode.DefaultDecodeHandlerManager;
import org.unidal.cat.transport.decode.NativeCommandDecodeHandler;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatClientModule;
import com.dianping.cat.analyzer.DataUploader;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.io.DefaultTransportManager;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(CatClientModule.class));
      all.add(A(CatClientInitializer.class));

      all.add(A(MessageIdFactory.class));
      all.add(A(com.dianping.cat.message.internal.MessageIdFactory.class));

      all.add(A(DefaultClientEnvironmentSettings.class));
      all.add(A(DefaultMessagePolicy.class));

      all.add(A(DefaultServerDiscovery.class));
      all.add(A(DefaultClientConfigurationManager.class));
      all.add(A(LocalClientConfigurationProvider.class));
      all.add(A(RemoteClientConfigurationProvider.class));

      all.add(A(PlainTextMessageCodec.class));
      all.add(A(NativeMessageCodec.class));
      all.add(A(NativeCommandCodec.class));

      all.add(A(TcpSocketStub.class));
      all.add(A(DefaultTransportConfiguration.class));
      all.add(A(DefaultSocketAddressProvider.class));

      all.add(A(DefaultClientTransportHub.class));
      all.add(A(DefaultDecodeHandlerManager.class));
      all.add(A(NativeCommandDecodeHandler.class));
      all.add(A(DefaultCommandDispatcher.class));

      all.add(A(DefaultMessageProducer.class));
      all.add(A(DefaultMessageManager.class));

      all.add(A(DefaultTransportManager.class));
      all.add(A(TcpSocketSender.class));

      all.add(A(DefaultMessageStatistics.class));

      all.add(A(StatusUpdateTask.class));
      all.add(A(DataUploader.class));

      return all;
   }
}
