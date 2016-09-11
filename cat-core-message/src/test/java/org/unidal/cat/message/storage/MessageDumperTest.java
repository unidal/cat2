package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MessageDumperTest extends ComponentTestCase {
   private MessageCodec m_codec;

   @Before
   public void before() {
      File baseDir = new File("target");

      Files.forDir().delete(new File(baseDir, "dump"), true);

      lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
      m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
   }

   @Test
   public void testRead() throws Exception {
      BucketManager manager = lookup(BucketManager.class, "local");
      String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

      for (int i = 0; i < 100000; i++) {
         Bucket bucket = manager.getBucket("mock", ip, 404259, true);
         MessageId id = new MessageId("mock", ip, 404259, i);

         try {
            ByteBuf buf = bucket.get(id);

            MessageTree tree = new DefaultMessageTree();

            m_codec.decode(buf, tree);

            Assert.assertEquals(id.toString(), tree.getMessageId());
         } catch (Exception e) {
            throw new Exception(String.format("Error when loading message(%s)! ", id), e);
         }
      }
   }

   @Test
   public void testWrite() throws Exception {
      MessageDumper dumper = lookup(MessageDumper.class);

      for (int i = 0; i < 100000; i++) {
         MessageId id = new MessageId("mock", "0a010203", 404259, i);
         MessageTree tree = TreeHelper.tree(m_codec, id);

         dumper.process(tree);
      }

      dumper.awaitTermination(404259);
   }
}
