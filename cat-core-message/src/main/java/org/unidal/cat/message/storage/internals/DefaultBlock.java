package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.unidal.cat.message.storage.Block;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;

public class DefaultBlock implements Block {
   private String m_domain;

   private int m_hour;

   private ByteBuf m_data;

   private int m_offset;

   private Map<MessageId, Integer> m_offsets = new LinkedHashMap<MessageId, Integer>();

   private OutputStream m_out;

   private boolean m_isFulsh;

   private static final int BUFFER_SIZE = 1024;

   public static int MAX_SIZE = 256 * 1024;

   public static CompressType COMMPRESS_TYPE = CompressType.SNAPPY;

   public static int DEFLATE_LEVEL = 5;

   private List<ByteBuf> m_bufs = new ArrayList<ByteBuf>(256);

   public DefaultBlock(MessageId id, int offset, byte[] data) {
      m_offsets.put(id, offset);
      m_data = data == null ? null : Unpooled.wrappedBuffer(data);
   }

   public DefaultBlock(String domain, int hour) {
      this(domain, hour, COMMPRESS_TYPE);
   }

   public DefaultBlock(String domain, int hour, CompressType type) {
      m_domain = domain;
      m_hour = hour;
      COMMPRESS_TYPE = type;
      m_data = Unpooled.buffer(8 * 1024);
      m_out = createOutputSteam(m_data, type);
   }

   @Override
   public void clear() {
      m_data = null;
      m_offsets.clear();
   }

   private InputStream createInputSteam(ByteBuf buf, CompressType type) {
      ByteBufInputStream os = new ByteBufInputStream(buf);
      InputStream in = null;

      if (type == CompressType.SNAPPY) {
         try {
            in = new SnappyInputStream(os);
         } catch (IOException e) {
            Cat.logError(e);
         }
      } else if (type == CompressType.GZIP) {
         try {
            in = new GZIPInputStream(os, BUFFER_SIZE);
         } catch (IOException e) {
            Cat.logError(e);
         }
      } else if (type == CompressType.DEFLATE) {
         Inflater inflater = new Inflater(true);

         in = new DataInputStream(new InflaterInputStream(os, inflater, BUFFER_SIZE));
      }
      return in;
   }

   private OutputStream createOutputSteam(ByteBuf buf, CompressType type) {
      ByteBufOutputStream os = new ByteBufOutputStream(buf);
      OutputStream out = null;

      if (type == CompressType.SNAPPY) {
         out = new SnappyOutputStream(os);
      } else if (type == CompressType.GZIP) {
         try {
            out = new GZIPOutputStream(os, BUFFER_SIZE);
         } catch (IOException e) {
            Cat.logError(e);
         }
      } else if (type == CompressType.DEFLATE) {
         out = new DeflaterOutputStream(os, new Deflater(DEFLATE_LEVEL, true), BUFFER_SIZE);
      }
      return out;
   }

   @Override
   public ByteBuf find(MessageId id) {
      Integer offset = m_offsets.get(id);

      if (offset != null) {
         finish();

         m_isFulsh = true;

         try {
            ByteBuf copyData = Unpooled.copiedBuffer(m_data);
            DataInputStream in = new DataInputStream(createInputSteam(copyData, COMMPRESS_TYPE));

            in.skip(offset);
            int length = in.readInt();
            byte[] result = new byte[length];

            in.readFully(result);

            return Unpooled.wrappedBuffer(result);
         } catch (IOException e) {
            Cat.logError(e);
         }
      }

      return null;
   }

   @Override
   public synchronized void finish() {
      try {
         for (ByteBuf buf : m_bufs) {
            int len = buf.readableBytes();

            writeInt(m_out, len);
            buf.readBytes(m_out, len);
         }

         m_bufs.clear();

         if (m_out != null) {
            m_out.flush();
            m_out.close();
            m_out = null;
         }
      } catch (IOException e) {
         Cat.logError(e);
      }
   }

   @Override
   public ByteBuf getData() throws IOException {
      return m_data;
   }

   @Override
   public String getDomain() {
      return m_domain;
   }

   @Override
   public int getHour() {
      return m_hour;
   }

   @Override
   public Map<MessageId, Integer> getOffsets() {
      return m_offsets;
   }

   @Override
   public boolean isFull() {
      return m_offset >= MAX_SIZE || m_isFulsh;
   }

   @Override
   public void pack(MessageId id, ByteBuf buf) throws IOException {
      int len = buf.readableBytes();

      m_bufs.add(buf);
      m_offsets.put(id, m_offset);
      m_offset += len + 4;
   }

   @Override
   public ByteBuf unpack(MessageId id) throws IOException {
      if (m_data == null) {
         return null;
      }

      DataInputStream in = new DataInputStream(createInputSteam(m_data, COMMPRESS_TYPE));
      Integer offset = m_offsets.get(id);

      if (offset == null) {
         return null;
      }

      in.skip(offset);

      int len = in.readInt();
      byte[] data = new byte[len];

      in.readFully(data);
      in.close();

      ByteBuf buf = Unpooled.wrappedBuffer(data);

      return buf;
   }

   private void writeInt(OutputStream out, int value) throws IOException {
      byte b0 = (byte) (value >>> 24);
      byte b1 = (byte) (value >>> 16);
      byte b2 = (byte) (value >>> 8);
      byte b3 = (byte) value;

      out.write(b0);
      out.write(b1);
      out.write(b2);
      out.write(b3);
   }
}
