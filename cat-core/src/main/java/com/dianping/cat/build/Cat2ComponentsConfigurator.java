package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.storage.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(LocalIndexManager.class));
		all.add(A(LocalIndex.class));

		all.add(A(LocalFileBuilder.class));
		all.add(A(LocalTokenMapping.class));
		all.add(A(LocalTokenMappingManager.class));
		all.add(A(DefaultStorageConfiguration.class));

		return all;
	}
}
