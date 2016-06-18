package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.DefaultClientTransportHub;
import org.unidal.cat.message.MessageIdFactory;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.cat.message.command.DefaultCommandDispatcher;
import org.unidal.cat.transport.DefaultSocketAddressProvider;
import org.unidal.cat.transport.DefaultTransportConfiguration;
import org.unidal.cat.transport.TcpSocketStub;
import org.unidal.cat.transport.decode.DefaultDecodeHandlerManager;
import org.unidal.cat.transport.decode.NativeCommandDecodeHandler;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultClientConfigManager.class));
		all.add(A(MessageIdFactory.class));

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

		return all;
	}
}
