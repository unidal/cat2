package org.unidal.cat.message.storage.local;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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

		long address = m_index.read(id);

		if (address < 0) {
			return new DefaultBlock(id, -1, null);
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;
			byte[] data = m_data.read(dataOffset);

			return new DefaultBlock(id, segmentOffset, data);
		}
	}

	@Override
	public void put(Block block) throws IOException {
		Map<MessageId, Integer> mappings = block.getMappings();
		ByteBuf data = block.getData();

		for (Map.Entry<MessageId, Integer> e : mappings.entrySet()) {
			MessageId id = e.getKey();
			int segmentOffset = e.getValue();

			ensureOpen(id);

			long dataOffset = m_data.getDataOffset();

			m_indexStopWatch.start();
			m_index.write(id, dataOffset, segmentOffset);
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

		private long m_offset;

		private Header m_header = new Header();

		private FileChannel m_channel;

		public void close() {
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
			int offset = m_header.getOffset(id.getIpAddressValue(), id.getIndex());

			m_file.seek(offset);

			try {
				long address = m_file.readLong();

				return address;
			} catch (EOFException e) {
				return -1;
			}
		}

		public void write(MessageId id, long dataOffset, int segmentOffset) throws IOException {
			int offset = m_header.getOffset(id.getIpAddressValue(), id.getIndex());
			ByteBuffer buf = ByteBuffer.wrap(new byte[8]);

			buf.mark();
			buf.putLong((dataOffset << 24) + segmentOffset);
			buf.reset();

			m_channel.position(offset);
			m_channel.write(buf);

			// m_indexFile.seek(offset);
			// m_indexFile.writeLong((dataOffset << 24) + segmentOffset);
		}

		private class Header {
			private static final String MAGIC_CODE = "CAT2 Local Index";

			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			public int getOffset(int ip, int seq) throws IOException {
				int segmentIndex = seq / MESSAGE_PER_SEGMENT;
				int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
				int segment = getOrCreateBlock(ip, segmentIndex);
				int offset = segment * SEGMENT_SIZE + segmentOffset;

				return offset;
			}

			private int getOrCreateBlock(int ip, int index) throws IOException {
				Map<Integer, Integer> segments = m_table.get(ip);

				if (segments == null) {
					segments = new HashMap<Integer, Integer>();
					m_table.put(ip, segments);
				}

				Integer segment = segments.get(index);

				if (segment == null) {
					segment = m_nextSegment++;
					segments.put(index, segment);
					m_file.seek(m_offset);
					m_file.writeInt(ip);
					m_file.writeInt(index);
					m_offset += 8;
				}

				return segment;
			}

			public void load() throws IOException {
				if (m_file.length() < 2 * SEGMENT_SIZE) {
					m_file.seek(0);
					m_file.write(MAGIC_CODE.getBytes());
					m_offset = 16;
					m_nextSegment = 2;
					return; // empty or invalid header
				}

				ByteBuf buf = Unpooled.buffer(2 * SEGMENT_SIZE);
				byte[] magic = new byte[16];

				m_file.seek(0);
				m_file.readFully(buf.array());

				buf.writerIndex(buf.capacity());
				buf.readBytes(magic);

				m_offset = 16;
				m_nextSegment = 2;

				if (!new String(magic).equals(MAGIC_CODE)) {
					throw new IOException("Invalid index file: " + m_path);
				}

				while (buf.isReadable()) {
					int ip = buf.readInt();
					int index = buf.readInt();

					if (ip != 0) {
						Map<Integer, Integer> segments = m_table.get(ip);

						if (segments == null) {
							segments = new HashMap<Integer, Integer>();
							m_table.put(ip, segments);
						}

						Integer segment = segments.get(index);

						if (segment == null) {
							segment = m_nextSegment++;
							segments.put(index, segment);
						}

						m_offset += 8;
					} else {
						break;
					}
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
