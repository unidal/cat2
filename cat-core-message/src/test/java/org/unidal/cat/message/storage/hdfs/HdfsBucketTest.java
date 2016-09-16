package org.unidal.cat.message.storage.hdfs;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class HdfsBucketTest extends ComponentTestCase {

   @Before
   public void before() throws Exception {
      ServerConfigManager config = lookup(ServerConfigManager.class);

      config.initialize(new File(HdfsBucketTest.class.getClassLoader().getResource("server.xml").getFile()));
   }

   @Test
   public void testManager() {
      HdfsBucketManager manager = lookup(HdfsBucketManager.class);

      for (int i = 0; i < 1000; i++) {
         MessageId id = MessageId.parse("cat-0a420d73-405915-" + i);

         MessageTree message = manager.loadMessage(id);

         if (message != null) {
            System.err.println(message);
         }
      }
   }

   @Test
   public void test() {
      Bucket hdfsbucket = lookup(Bucket.class, "hdfs");
      MessageCodec m_plainText = lookup(MessageCodec.class, PlainTextMessageCodec.ID);

      try {
         MessageId id = MessageId.parse("shop-web-0a420d56-405915-16");
         hdfsbucket.initialize(id.getDomain(), "10.66.13.115", id.getHour());

         ByteBuf byteBuf = hdfsbucket.get(id);

         if (byteBuf != null) {
            MessageTree tree = new DefaultMessageTree();

            m_plainText.decode(byteBuf, tree);
            System.out.println(tree.toString());
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
