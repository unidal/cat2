package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.MessageIdFactory;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.codec.NativeMessageCodec;
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

		return all;
	}
}
