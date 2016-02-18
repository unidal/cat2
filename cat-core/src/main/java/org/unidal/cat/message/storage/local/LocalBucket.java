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

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.FileBuilder.FileType;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;

@Named(type = Bucket.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalBucket implements Bucket {
	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject("local")
	private FileBuilder m_bulider;

	private StopWatch m_indexStopWatch;

	private StopWatch m_dataStopWatch;

	private DataHelper m_data = new DataHelper();

	private IndexHelper m_index = new IndexHelper();

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_indexStopWatch.start();
			m_index.close();
			m_indexStopWatch.stop();

			m_dataStopWatch.start();
			m_data.close();
			m_dataStopWatch.stop();

			System.out.println(String.format("%s, %s", m_indexStopWatch, m_dataStopWatch));
		}
	}

	private void ensureOpen(MessageId id) throws IOException {
		if (!m_index.isOpen()) {
			String domain = id.getDomain();
			long timestamp = id.getTimestamp();
			Date startTime = new Date(timestamp);
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			File dataPath = m_bulider.getFile(domain, startTime, ip, FileType.DATA);
			File indexPath = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);

			m_indexStopWatch = new StopWatch("index-" + domain);
			m_dataStopWatch = new StopWatch("data-" + domain);
			m_data.init(dataPath);
			m_index.init(indexPath);
		}
	}

	@Override
	public Block get(MessageId id) throws IOException {
		ensureOpen(id);

		m_indexStopWatch.start();
		long address = m_index.read(id);
		m_indexStopWatch.stop();

		if (address < 0) {
			return new DefaultBlock(id, -1, null);
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;

			m_dataStopWatch.start();
			byte[] data = m_data.read(dataOffset);
			m_dataStopWatch.stop();

			return new DefaultBlock(id, segmentOffset, data);
		}
	}

	@Override
	public void put(Block block) throws IOException {
		Map<MessageId, Integer> mappings = block.getMappings();
		ByteBuf data = block.getData();

		for (Map.Entry<MessageId, Integer> e : mappings.entrySet()) {
			MessageId id = e.getKey();
			int offset = e.getValue();

			ensureOpen(id);

			long dataOffset = m_data.getDataOffset();

			m_indexStopWatch.start();
			m_index.write(id, dataOffset, offset);
			m_indexStopWatch.stop();
		}

		m_dataStopWatch.start();
		m_data.write(data);
		m_dataStopWatch.stop();
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

		private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

		private RandomAccessFile m_file;

		private File m_path;

		private int m_offset;

		private FileChannel m_channel;

		private Header m_header = new Header();

		private Map<Long, Segment> m_segments = new HashMap<Long, Segment>();

		public void close() {
			try {
				m_header.m_segment.flush();

				for (Segment segment : m_segments.values()) {
					segment.flush();
				}
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_channel.force(false);
				m_channel.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_file = null;
		}

		private Segment getSegment(long id) throws IOException {
			Segment segment = m_segments.get(id);

			if (segment == null) {
				segment = new Segment(m_channel, id * SEGMENT_SIZE);
				m_segments.put(id, segment);
			}

			return segment;
		}

		public void init(File indexPath) throws IOException {
			m_path = indexPath;
			m_path.getParentFile().mkdirs();
			m_file = new RandomAccessFile(m_path, "rwd"); // read-write without meta sync
			m_channel = m_file.getChannel();

			m_header.load();
		}

		public boolean isOpen() {
			return m_file != null;
		}

		public long read(MessageId id) throws IOException {
			int position = m_header.getOffset(id.getIpAddressValue(), id.getIndex(), false);

			if (position != -1) {
				int segmentId = position / SEGMENT_SIZE;
				int offset = position % SEGMENT_SIZE;
				Segment segment = getSegment(segmentId);

				if (segment != null) {
					try {
						long blockAddress = segment.readLong(offset);

						return blockAddress;
					} catch (EOFException e) {
						// ignore it
					}
				}
			}

			return -1;
		}

		public void write(MessageId id, long blockAddress, int blockOffset) throws IOException {
			int position = m_header.getOffset(id.getIpAddressValue(), id.getIndex(), true);
			int address = position / SEGMENT_SIZE;
			int offset = position % SEGMENT_SIZE;
			Segment segment = getSegment(address);

			segment.writeLong(offset, (blockAddress << 24) + blockOffset);
		}

		private class Header {
			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			private Segment m_segment;

			private Integer findSegment(int ip, int index, boolean createIfNotExists) throws IOException {
				Map<Integer, Integer> map = m_table.get(ip);

				if (map == null && createIfNotExists) {
					map = new HashMap<Integer, Integer>();
					m_table.put(ip, map);
				}

				Integer segmentId = map == null ? null : map.get(index);

				if (segmentId == null && createIfNotExists) {
					long value = (((long) ip) << 32) + index;

					segmentId = m_nextSegment++;
					map.put(index, segmentId);
					m_segment.writeLong(m_offset, value);
					m_offset += 8;
				}

				return segmentId;
			}

			public int getOffset(int ip, int seq, boolean createIfNotExists) throws IOException {
				int segmentIndex = seq / MESSAGE_PER_SEGMENT;
				int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
				Integer segmentId = findSegment(ip, segmentIndex, createIfNotExists);

				if (segmentId != null) {
					int offset = segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

					return offset;
				} else {
					return -1;
				}
			}

			public void load() throws IOException {
				Segment segment = new Segment(m_channel, 0);
				long magicCode = segment.readLong();

				if (magicCode == 0) {
					segment.writeLong(0, -1);
				} else if (magicCode != -1) {
					throw new IOException("Invalid index file: " + m_path);
				}

				m_nextSegment = 1;
				m_offset = 8;

				while (true) {
					int ip = segment.readInt();
					int index = segment.readInt();

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

				m_segment = segment;
			}
		}

		private class Segment {
			private FileChannel m_channel;

			private long m_address;

			private ByteBuffer m_buf;

			private long m_lastAccessTime;

			private boolean m_dirty;

			private Segment(FileChannel channel, long address) throws IOException {
				m_channel = channel;
				m_address = address;
				m_lastAccessTime = System.currentTimeMillis();
				m_buf = ByteBuffer.allocate(SEGMENT_SIZE);
				m_buf.mark();
				m_channel.read(m_buf, address);
				m_buf.reset();
			}

			public void flush() throws IOException {
				if (m_dirty) {
					int pos = m_buf.position();

					m_buf.position(0);
					m_channel.write(m_buf, m_address);
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

				if (m_lastAccessTime + 100 * 1000L < System.currentTimeMillis()) { // idle after 1 second
					flush();
				}
			}
		}
	}

	private static class StopWatch {
		private String m_name;

		private long m_sum;

		private long m_start;

		public StopWatch(String name) {
			m_name = name;
		}

		public void start() {
			m_start = System.nanoTime();
		}

		public void stop() {
			m_sum += System.nanoTime() - m_start;
		}

		@Override
		public String toString() {
			return String.format("%s: %s ms", m_name, m_sum / 1000000L);
		}
	}
}
