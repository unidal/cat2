package org.unidal.cat.core.message.service;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageService.class)
public class DefaultMessageService implements MessageService {
   @Inject
   private MessageFinderManager m_finderManager;

   @Inject("local")
   private BucketManager m_localBucketManager;

   @Inject(NativeMessageCodec.ID)
   private MessageCodec m_nativeCodec;

   @Override
   public MessageTree getMessageTree(MessageId id) throws IOException {
      ByteBuf buf = m_finderManager.find(id);

      if (buf == null) {
         Bucket bucket = m_localBucketManager.getBucket(id.getDomain(), id.getIpAddress(), id.getHour(), false);

         if (bucket != null) {
            bucket.flush();

            buf = bucket.get(id);
         }
      }

      if (buf != null) {
         MessageTree tree = new DefaultMessageTree();

         m_nativeCodec.decode(buf, tree);
         return tree;
      } else {
         return null;
      }
   }
}
