package org.unidal.cat.core.message.service;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageService.class)
public class LocalMessageService implements MessageService {
   @Inject
   private MessageFinderManager m_finderManager;

   @Inject("local")
   private BucketManager m_localBucketManager;

   @Inject
   private MessageCodecService m_codec;

   @Override
   public MessageTree getMessageTree(MessageId id) throws IOException {
      ByteBuf buf = m_finderManager.find(id);

      if (buf == null) {
         Bucket bucket = m_localBucketManager.getBucket(id.getDomain(), id.getIpAddressInHex(), id.getHour(), false);

         if (bucket != null) {
            bucket.flush();

            buf = bucket.get(id);

            if (buf != null) {
               Cat.logEvent("LogTree.Source", "File");
            }
         }
      } else {
         if (buf != null) {
            Cat.logEvent("LogTree.Source", "Memory");
         }
      }

      if (buf != null) {
         return m_codec.decodeNative(buf);
      } else {
         Cat.logEvent("LogTree.Source", "Missing");
         return null;
      }
   }
}
