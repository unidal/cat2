package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.cat.metric.DefaultBenchmarkManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultMessageDumperManager.class));
		all.add(A(DefaultMessageFinderManager.class));
		all.add(A(DefaultMessageDumper.class));
		all.add(A(DefaultMessageProcessor.class));
		all.add(A(DefaultBlockDumperManager.class));
		all.add(A(DefaultBlockDumper.class));
		all.add(A(DefaultBlockWriter.class));

		all.add(A(DefaultBenchmarkManager.class));
		
		all.add(A(LocalBucketManager.class));
		all.add(A(LocalBucket.class));
		all.add(A(LocalIndexManager.class));
		all.add(A(LocalIndex.class));

		all.add(A(LocalFileBuilder.class));
		all.add(A(LocalTokenMapping.class));
		all.add(A(LocalTokenMappingManager.class));

		all.add(A(DefaultStorageConfiguration.class));

		return all;
	}
}
