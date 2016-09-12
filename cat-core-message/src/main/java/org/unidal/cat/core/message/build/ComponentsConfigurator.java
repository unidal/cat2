package org.unidal.cat.core.message.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.message.codec.HtmlEncodingBufferWriter;
import org.unidal.cat.core.message.codec.HtmlMessageCodec;
import org.unidal.cat.core.message.codec.WaterfallMessageCodec;
import org.unidal.cat.core.message.config.DefaultMessageConfiguration;
import org.unidal.cat.core.message.service.DefaultMessageService;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsFileBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.hdfs.HdfsIndexManager;
import org.unidal.cat.message.storage.hdfs.HdfsMessageConsumerFinder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMapping;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMappingManager;
import org.unidal.cat.message.storage.hdfs.HdfsUploader;
import org.unidal.cat.message.storage.hdfs.LogviewProcessor;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultByteBufPool;
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
import org.unidal.cat.service.internals.DefaultCompressionService;
import org.unidal.cat.service.internals.GzipCompressionService;
import org.unidal.cat.service.internals.ZlibCompressionService;
import org.unidal.cat.spi.command.DefaultCommandDispatcher;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.TcpSocketReceiver;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   public List<Component> defineCodecComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(HtmlEncodingBufferWriter.class));
      all.add(A(HtmlMessageCodec.class));
      all.add(A(WaterfallMessageCodec.class));

      return all;
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultMessageConfiguration.class));
      all.add(A(DefaultMessageService.class));

      all.add(A(DefaultBenchmarkManager.class));

      all.addAll(defineCodecComponents());
      all.addAll(defineServiceComponents());
      all.addAll(defineStorageComponents());
      all.addAll(defineTransportComponents());

      // Please keep it as last
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }

   private List<Component> defineServiceComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultCompressionService.class));
      all.add(A(GzipCompressionService.class));
      all.add(A(ZlibCompressionService.class));

      return all;
   }

   private List<Component> defineStorageComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(DefaultMessageDumperManager.class));
      all.add(A(DefaultMessageFinderManager.class));
      all.add(A(DefaultMessageDumper.class));
      all.add(A(DefaultMessageProcessor.class));
      all.add(A(DefaultBlockDumperManager.class));
      all.add(A(DefaultBlockDumper.class));
      all.add(A(DefaultBlockWriter.class));

      all.add(A(HdfsSystemManager.class));

      all.add(A(HdfsMessageConsumerFinder.class));

      all.add(A(LocalBucket.class));
      all.add(A(LocalBucketManager.class));
      all.add(A(HdfsBucket.class));
      all.add(A(HdfsBucketManager.class));

      all.add(A(LocalIndex.class));
      all.add(A(LocalIndexManager.class));
      all.add(A(HdfsIndex.class));
      all.add(A(HdfsIndexManager.class));

      all.add(A(LocalFileBuilder.class));
      all.add(A(HdfsFileBuilder.class));
      all.add(A(LocalTokenMapping.class));
      all.add(A(HdfsTokenMapping.class));
      all.add(A(LocalTokenMappingManager.class));
      all.add(A(HdfsTokenMappingManager.class));

      all.add(A(DefaultStorageConfiguration.class));
      all.add(A(DefaultByteBufPool.class));

      all.add(A(HdfsUploader.class));
      all.add(A(LogviewProcessor.class));

      return all;
   }

   private List<Component> defineTransportComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(TcpSocketReceiver.class)); // TODO remove it later

      all.add(A(DefaultCommandDispatcher.class));

      return all;
   }
}
