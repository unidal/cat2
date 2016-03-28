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
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.FileBuilder.FileType;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkEnabled;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Bucket.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalBucket implements Bucket, BenchmarkEnabled {
	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject("local")
	private FileBuilder m_bulider;

	private Metric m_indexMetric;

	private Metric m_dataMetric;

	private DataHelper m_data = new DataHelper();

	private IndexHelper m_index = new IndexHelper();

	private Benchmark m_benchmark;

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_indexMetric.start();
			m_index.close();
			m_indexMetric.end();

			m_dataMetric.start();
			m_data.close();
			m_dataMetric.end();
		}
	}

	public void flush() {
		try {
			m_data.m_file.getFD().sync();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public ByteBuf get(MessageId id) throws IOException {
		m_indexMetric.start();
		long address = m_index.read(id);
		m_indexMetric.end();

		if (address < 0) {
			return null;
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;

			m_dataMetric.start();
			byte[] data = m_data.read(dataOffset);
			m_dataMetric.end();

			DefaultBlock block = new DefaultBlock(id, segmentOffset, data);

			return block.unpack(id);
		}
	}

	@Override
   public Benchmark getBechmark() {
		return m_benchmark;
   }

	@Override
	public void initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		File dataPath = m_bulider.getFile(domain, startTime, ip, FileType.DATA);
		File indexPath = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);

		m_dataMetric.start();
		m_data.init(dataPath);
		m_dataMetric.end();

		m_indexMetric.start();
		m_index.init(indexPath);
		m_indexMetric.end();
	}

	@Override
	public void puts(ByteBuf data, Map<MessageId, Integer> mappings) throws IOException {
		long dataOffset = m_data.getDataOffset();

		m_dataMetric.start();
		m_data.write(data);
		m_dataMetric.end();

		for (Map.Entry<MessageId, Integer> e : mappings.entrySet()) {
			MessageId id = e.getKey();
			int offset = e.getValue();

			m_indexMetric.start();
			m_index.write(id, dataOffset, offset);
			m_indexMetric.end();
		}
	}

	@Override
	public void setBenchmark(Benchmark benchmark) {
		m_benchmark = benchmark;
		m_indexMetric = benchmark.get("index");
		m_dataMetric = benchmark.get("data");
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

		public void close() {
			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				if (m_out != null) {
					m_out.close();
				}
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

			m_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_path, true), SEGMENT_SIZE));
			m_file = new RandomAccessFile(m_path, "r"); // read-only
			m_offset = m_path.length();
		}

		public byte[] read(long dataOffset) throws IOException {
			m_out.flush();
			m_file.seek(dataOffset);

			int len = m_file.readInt();
			byte[] data = new byte[len];

			m_file.readFully(data);

			return data;
		}

		public void write(ByteBuf data) throws IOException {
			int len = data.readableBytes();

			m_out.writeInt(len);
			data.readBytes(m_out, len);
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
				m_header.m_segment.flush();

				for (SegmentCache cache : m_caches.values()) {
					cache.flush();
				}
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_indexChannel.force(false);
				m_indexChannel.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_file = null;
			m_caches.clear();
		}

		private Segment getSegment(String ip, long id) throws IOException {
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
			m_file = new RandomAccessFile(m_path, "rwd");
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

			int segmentId = (int) (position / SEGMENT_SIZE);
			int offset = (int) (position % SEGMENT_SIZE);
			Segment segment = getSegment(id.getIpAddress(), segmentId);

			if (segment != null) {
				try {
					long blockAddress = segment.readLong(offset);

					return blockAddress;
				} catch (EOFException e) {
					// ignore it
				}
			} else {
				m_file.seek(position);

				long address = m_file.readLong();

				return address;
			}

			return -1;
		}

		public void write(MessageId id, long blockAddress, int blockOffset) throws IOException {
			long position = m_header.getOffset(id.getIpAddressValue(), id.getIndex(), true);
			long address = position / SEGMENT_SIZE;
			int offset = (int) (position % SEGMENT_SIZE);
			Segment segment = getSegment(id.getIpAddress(), address);
			long value = (blockAddress << 24) + blockOffset;

			if (segment != null) {
				segment.writeLong(offset, value);
			} else {
				Cat.logEvent("Block", "Abnormal:" + id.getDomain(), Event.SUCCESS, id.toString());
				m_indexChannel.position(position);

				ByteBuffer buf = ByteBuffer.allocate(8);
				buf.putLong(value);
				buf.flip();
				m_indexChannel.write(buf);
			}
		}

		private class Header {
			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			private Segment m_segment;

			private int m_offset;

			private Integer findSegment(int ip, int index, boolean createIfNotExists) throws IOException {
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
						m_segment.flush();
						m_segment = new Segment(m_indexChannel, m_nextSegment * SEGMENT_SIZE);

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
					long offset = segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

					return offset;
				} else {
					return -1;
				}
			}

			public void load(int headBlockIndex) throws IOException {
				Segment segment = new Segment(m_indexChannel, headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
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

			private boolean m_dirty;

			private Segment(FileChannel channel, long address) throws IOException {
				m_segmentChannel = channel;
				m_address = address;
				m_buf = ByteBuffer.allocate(SEGMENT_SIZE);
				m_buf.mark();
				m_segmentChannel.read(m_buf, address);
				m_buf.reset();
			}

			public void flush() throws IOException {
				if (m_dirty) {
					int pos = m_buf.position();

					m_buf.position(0);
					m_segmentChannel.write(m_buf, m_address);
					m_buf.position(pos);
					m_dirty = false;
				}
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
				m_dirty = true;
			}
		}

		private class SegmentCache {
			private long m_maxSegmentId;

			private Map<Long, Segment> m_latestSegments = new LinkedHashMap<Long, Segment>();

			private final static int CACHE_SIZE = 2;

			public Segment findOrCreateNextSegment(long segmentId) throws IOException {
				Segment segment = m_latestSegments.get(segmentId);

				if (segment == null) {
					if (segmentId > m_maxSegmentId) {
						if (m_latestSegments.size() >= CACHE_SIZE) {
							removeOldSegment();
						}

						segment = new Segment(m_indexChannel, segmentId * SEGMENT_SIZE);

						m_latestSegments.put(segmentId, segment);
						m_maxSegmentId = segmentId;
					} else {
						Cat.logEvent("OldSegment", String.valueOf(segmentId) + ",max:" + String.valueOf(m_maxSegmentId));
					}
				}

				return segment;
			}

			public void flush() throws IOException {
				for (Segment segment : m_latestSegments.values()) {
					segment.flush();
				}
				m_latestSegments.clear();
			}

			private void removeOldSegment() throws IOException {
				Entry<Long, Segment> first = m_latestSegments.entrySet().iterator().next();

				first.getValue().flush();

				m_latestSegments.remove(first.getKey());
			}
		}
	}

}
