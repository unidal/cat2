package org.unidal.cat.message.storage.local;

import io.netty.buffer.ByteBuf;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.cat.message.storage.internals.ByteBufPool;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Bucket.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalBucket implements Bucket {
   private static final int SEGMENT_SIZE = 32 * 1024;

   @Inject("local")
   private PathBuilder m_bulider;

   @Inject
   private ByteBufPool m_bufCache;

   @Inject
   private ServerConfigManager m_config;

   private DataHelper m_data = new DataHelper();

   private IndexHelper m_index = new IndexHelper();

   private boolean m_nioEnabled = true;

   @Override
   public void close() {
      if (m_index.isOpen()) {
         m_index.close();
         m_data.close();
      }
   }

   @Override
   public void flush() {
      try {
         if (!m_nioEnabled) {
            m_data.m_out.flush();
         }
         m_data.m_file.getFD().sync();
      } catch (Exception e) {
         Cat.logError(e);
      }
   }

   @Override
   public ByteBuf get(MessageId id) throws IOException {
      long address = m_index.read(id);

      if (address <= 0) {
         return null;
      } else {
         int segmentOffset = (int) (address & 0xFFFFFFL);
         long dataOffset = address >> 24;
         byte[] data = m_data.read(dataOffset);

         DefaultBlock block = new DefaultBlock(id, segmentOffset, data);

         return block.unpack(id);
      }
   }

   @Override
   public void initialize(String domain, String ip, int hour) throws IOException {
      m_nioEnabled = m_config.getStroargeNioEnable();
      long timestamp = hour * 3600 * 1000L;
      Date startTime = new Date(timestamp);
      File indexPath = new File(m_bulider.getPath(domain, startTime, ip, FileType.INDEX));
      File dataPath = new File(m_bulider.getPath(domain, startTime, ip, FileType.DATA));

      m_index.init(indexPath);
      m_data.init(dataPath);
   }

   @Override
   public synchronized void puts(ByteBuf data, Map<MessageId, Integer> mappings) throws IOException {
      long dataOffset = m_data.getDataOffset();

      m_data.write(dataOffset, data);

      for (Map.Entry<MessageId, Integer> e : mappings.entrySet()) {
         MessageId id = e.getKey();
         int offset = e.getValue();

         m_index.write(id, dataOffset, offset);
      }
   }

   @Override
   public String toString() {
      return String.format("%s[%s]", getClass().getSimpleName(), m_data.getPath());
   }

   private class DataHelper {
      private File m_path;

      private RandomAccessFile m_file;

      private long m_offset;

      private DataOutputStream m_out;

      private FileChannel m_dataChannel;

      public void close() {
         if (m_nioEnabled) {
            try {
               m_dataChannel.close();
            } catch (IOException e) {
               Cat.logError(e);
            }
         } else {
            try {
               if (m_out != null) {
                  m_out.close();
               }
            } catch (IOException e) {
               Cat.logError(e);
            }
         }

         try {
            m_file.close();
         } catch (IOException e) {
            Cat.logError(e);
         }

         m_file = null;
      }

      public long getDataOffset() {
         return m_offset;
      }

      public File getPath() {
         return m_path;
      }

      public void init(File dataPath) throws IOException {
         m_path = dataPath;
         m_path.getParentFile().mkdirs();

         m_file = new RandomAccessFile(m_path, "rw"); // read-write
         m_offset = m_path.length();

         if (m_nioEnabled) {
            m_dataChannel = m_file.getChannel();
            m_dataChannel.position(m_offset);

            if (m_offset == 0) {
               ByteBuffer buf = ByteBuffer.allocate(4);
               buf.putInt(-1);
               buf.flip();
               m_dataChannel.write(buf);
               m_offset += 4;
            }
         } else {
            m_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_path, true), SEGMENT_SIZE));

            if (m_offset == 0) {
               m_out.writeInt(-1);
               m_offset += 4;
            }
         }
      }

      public byte[] read(long dataOffset) throws IOException {
         m_file.seek(dataOffset);

         int len = m_file.readInt();
         byte[] data = new byte[len];

         m_file.readFully(data);

         return data;
      }

      public void write(long offset, ByteBuf data) throws IOException {
         int len = data.readableBytes();

         if (m_nioEnabled) {
            m_dataChannel.position(offset);
            ByteBuffer buf = ByteBuffer.allocate(4 + len);

            buf.putInt(len);
            buf.put(data.array(), 0, len);
            buf.flip();
            m_dataChannel.write(buf);
         } else {
            m_out.writeInt(len);
            data.readBytes(m_out, len);
         }

         m_offset += len + 4;
      }
   }

   private class IndexHelper {
      private static final int BYTE_PER_MESSAGE = 8;

      private static final int BYTE_PER_ENTRY = 8;

      private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

      private static final int ENTRY_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_ENTRY;

      private RandomAccessFile m_file;

      private File m_path;

      private FileChannel m_indexChannel;

      private Header m_header = new Header();

      private Map<String, SegmentCache> m_caches = new LinkedHashMap<String, SegmentCache>();

      public void close() {
         try {
            m_header.m_segment.close();

            for (SegmentCache cache : m_caches.values()) {
               cache.close();
            }
         } catch (IOException e) {
            Cat.logError(e);
         }

         if (m_nioEnabled) {
            try {
               m_indexChannel.force(false);
               m_indexChannel.close();
            } catch (IOException e) {
               Cat.logError(e);
            }
         } else {
         }

         try {
            m_file.close();
         } catch (IOException e) {
            Cat.logError(e);
         }

         m_file = null;
         m_caches.clear();
      }

      public Segment getSegment(String ip, long id) throws IOException {
         SegmentCache cache = m_caches.get(ip);

         if (cache == null) {
            cache = new SegmentCache();
            m_caches.put(ip, cache);
         }

         return cache.findOrCreateNextSegment(id);
      }

      public void init(File indexPath) throws IOException {
         m_path = indexPath;
         m_path.getParentFile().mkdirs();

         // read-write without meta sync
         m_file = new RandomAccessFile(m_path, "rw");
         m_indexChannel = m_file.getChannel();

         long size = m_file.length();
         int totalHeaders = (int) Math.ceil((size * 1.0 / (ENTRY_PER_SEGMENT * SEGMENT_SIZE)));

         if (totalHeaders == 0) {
            totalHeaders = 1;
         }

         for (int i = 0; i < totalHeaders; i++) {
            m_header.load(i);
         }
      }

      public boolean isOpen() {
         return m_file != null;
      }

      public long read(MessageId id) throws IOException {
         int index = id.getIndex();
         long position = m_header.getOffset(id.getIpAddressValue(), index, false);

         long segmentId = position / SEGMENT_SIZE;
         int offset = (int) (position % SEGMENT_SIZE);
         Segment segment = getSegment(id.getIpAddressInHex(), segmentId);

         if (segment != null) {
            try {
               long blockAddress = segment.readLong(offset);

               return blockAddress;
            } catch (EOFException e) {
               // ignore it
            }
         } else if (position > 0) {
            m_file.seek(position);

            long address = m_file.readLong();

            return address;
         }

         return -1;
      }

      public void write(MessageId id, long blockAddress, int blockOffset) throws IOException {
         long position = m_header.getOffset(id.getIpAddressValue(), id.getIndex(), true);
         long segmentId = position / SEGMENT_SIZE;
         int offset = (int) (position % SEGMENT_SIZE);
         Segment segment = getSegment(id.getIpAddressInHex(), segmentId);
         long value = (blockAddress << 24) + blockOffset;

         if (segment != null) {
            segment.writeLong(offset, value);
         } else {
            Cat.logEvent("Block", "Abnormal:" + id.getDomain(), Event.SUCCESS, null);
            if (m_nioEnabled) {
               m_indexChannel.position(position);

               ByteBuffer buf = ByteBuffer.allocate(8);
               buf.putLong(value);
               buf.flip();
               m_indexChannel.write(buf);
            } else {
               m_file.seek(position);
               m_file.writeLong(value);
            }
         }
      }

      private class Header {
         private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

         private int m_nextSegment;

         private Segment m_segment;

         private int m_offset;

         public Integer findSegment(int ip, int index, boolean createIfNotExists) throws IOException {
            Map<Integer, Integer> map = m_table.get(ip);

            if (map == null && createIfNotExists) {
               map = new HashMap<Integer, Integer>();
               m_table.put(ip, map);
            }

            Integer segmentId = map == null ? null : map.get(index);

            if (segmentId == null && createIfNotExists) {
               long value = (((long) ip) << 32) + index;

               segmentId = m_nextSegment;
               map.put(index, segmentId);

               m_segment.writeLong(m_offset, value);
               m_offset += 8;

               m_nextSegment++;

               if (m_nextSegment % (ENTRY_PER_SEGMENT) == 0) {
                  // last segment is full, create new one
                  m_segment.close();
                  m_segment = new Segment(m_indexChannel, 1L * m_nextSegment * SEGMENT_SIZE);

                  m_nextSegment++; // skip self head data
                  m_segment.writeLong(0, -1);
                  m_offset = 8;
               }
            }

            return segmentId;
         }

         public long getOffset(int ip, int seq, boolean createIfNotExists) throws IOException {
            int segmentIndex = seq / MESSAGE_PER_SEGMENT;
            int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
            Integer segmentId = findSegment(ip, segmentIndex, createIfNotExists);

            if (segmentId != null) {
               long offset = 1L * segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

               return offset;
            } else {
               return -1;
            }
         }

         public void load(int headBlockIndex) throws IOException {
            Segment segment = new Segment(m_indexChannel, 1L * headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
            long magicCode = segment.readLong();

            if (magicCode == 0) {
               segment.writeLong(0, -1);
            } else if (magicCode != -1) {
               throw new IOException("Invalid index file: " + m_path);
            }

            m_segment = segment;
            m_nextSegment = 1 + ENTRY_PER_SEGMENT * headBlockIndex;
            m_offset = 8;

            int readerIndex = 1;

            while (readerIndex < ENTRY_PER_SEGMENT) {
               int ip = segment.readInt();
               int index = segment.readInt();

               readerIndex++;

               if (ip != 0) {
                  Map<Integer, Integer> map = m_table.get(ip);

                  if (map == null) {
                     map = new HashMap<Integer, Integer>();
                     m_table.put(ip, map);
                  }

                  Integer segmentNo = map.get(index);

                  if (segmentNo == null) {
                     segmentNo = m_nextSegment++;

                     map.put(index, segmentNo);
                  }

                  m_offset += 8;
               } else {
                  break;
               }
            }
         }
      }

      private class Segment {
         private FileChannel m_segmentChannel;

         private long m_address;

         private ByteBuffer m_buf;

         public Segment(FileChannel channel, long address) throws IOException {
            m_segmentChannel = channel;
            m_address = address;

            m_buf = m_bufCache.get();
            m_buf.mark();
            m_segmentChannel.read(m_buf, address);
            m_buf.reset();
         }

         public void close() throws IOException {
            int pos = m_buf.position();

            m_buf.position(0);
            m_segmentChannel.write(m_buf, m_address);
            m_buf.position(pos);
            m_bufCache.put(m_buf);
         }

         public int readInt() throws IOException {
            return m_buf.getInt();
         }

         public long readLong() throws IOException {
            return m_buf.getLong();
         }

         public long readLong(int offset) throws IOException {
            return m_buf.getLong(offset);
         }

         @Override
         public String toString() {
            return String.format("%s[address=%s]", getClass().getSimpleName(), m_address);
         }

         public void writeLong(int offset, long value) throws IOException {
            m_buf.putLong(offset, value);
         }
      }

      private class SegmentCache {
         private long m_maxSegmentId;

         private Map<Long, Segment> m_latestSegments = new LinkedHashMap<Long, Segment>();

         private final static int CACHE_SIZE = 2;

         public void close() throws IOException {
            for (Segment segment : m_latestSegments.values()) {
               segment.close();
            }
            m_latestSegments.clear();
         }

         public Segment findOrCreateNextSegment(long segmentId) throws IOException {
            Segment segment = m_latestSegments.get(segmentId);

            if (segment == null) {
               if (segmentId > m_maxSegmentId) {
                  if (m_latestSegments.size() >= CACHE_SIZE) {
                     removeOldSegment();
                  }

                  segment = new Segment(m_indexChannel, 1L * segmentId * SEGMENT_SIZE);

                  m_latestSegments.put(segmentId, segment);
                  m_maxSegmentId = segmentId;
               } else {
                  int duration = (int) (m_maxSegmentId - segmentId);
                  Cat.logEvent("OldSegment", String.valueOf(duration), Event.SUCCESS, String.valueOf(segmentId)
                        + ",max:" + String.valueOf(m_maxSegmentId));
               }
            }

            return segment;
         }

         public void removeOldSegment() throws IOException {
            Entry<Long, Segment> first = m_latestSegments.entrySet().iterator().next();
            Segment segment = m_latestSegments.remove(first.getKey());

            segment.close();
         }
      }
   }

}
