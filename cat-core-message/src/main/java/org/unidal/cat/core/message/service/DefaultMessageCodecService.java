package org.unidal.cat.core.message.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.unidal.cat.core.message.codec.HtmlMessageCodec;
import org.unidal.cat.core.message.codec.WaterfallMessageCodec;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageCodecService.class)
public class DefaultMessageCodecService implements MessageCodecService {
   @Inject(NativeMessageCodec.ID)
   private MessageCodec m_native;

   @Inject(HtmlMessageCodec.ID)
   private MessageCodec m_html;

   @Inject(WaterfallMessageCodec.ID)
   private MessageCodec m_waterfall;

   @Override
   public MessageTree decodeNative(byte[] content) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(content.length);

      buf.writeBytes(content);
      return decodeNative(buf);
   }

   @Override
   public MessageTree decodeNative(ByteBuf buf) {
      MessageTree tree = new DefaultMessageTree();

      m_native.decode(buf, tree);
      return tree;
   }

   @Override
   public ByteBuf encodeHtml(MessageTree tree) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

      m_html.encode(tree, buf);
      return buf;
   }

   @Override
   public ByteBuf encodeNative(MessageTree tree) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

      m_native.encode(tree, buf);
      return buf;
   }

   @Override
   public ByteBuf encodeWaterfall(MessageTree tree) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

      m_waterfall.encode(tree, buf);
      return buf;
   }
}
