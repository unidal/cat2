package org.unidal.cat.core.message.service;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageCodecService {
   public MessageTree decodeNative(byte[] content);

   public MessageTree decodeNative(ByteBuf buf);

   public ByteBuf encodeHtml(MessageTree tree);

   public ByteBuf encodeNative(MessageTree tree);

   public ByteBuf encodeWaterfall(MessageTree tree);
}
