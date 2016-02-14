package org.unidal.cat.message.storage.local;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

@Named(type = Bucket.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalBucket implements Bucket {
	private static final int BLOCK_SIZE = 32 * 1024;

	private static final int BYTE_PER_MESSAGE = 8;

	private static final int MESSAGE_PER_BLOCK = BLOCK_SIZE / BYTE_PER_MESSAGE;

	@Inject("local")
	private FileBuilder m_bulider;

	private RandomAccessFile m_indexFile;

	private RandomAccessFile m_dataFile;

	private File m_indexPath;

	private File m_dataPath;

	private long m_dataOffset;

	private long m_indexOffset;

	private IndexHeader m_indexHeader = new IndexHeader();

	@Override
	public void close() {
		if (m_indexFile != null) {
			try {
				m_dataFile.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_indexFile.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_dataFile = null;
			m_indexFile = null;
		}
	}

	private void ensureOpen(MessageId id) throws IOException {
		if (m_indexFile == null) {
			String domain = id.getDomain();
			long timestamp = id.getTimestamp();
			String ip = id.getIpAddress();
			Date startTime = new Date(timestamp);

			m_indexPath = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);
			m_dataPath = m_bulider.getFile(domain, startTime, ip, FileType.DATA);
			m_indexPath.getParentFile().mkdirs();
			m_dataPath.getParentFile().mkdirs();
			m_indexFile = new RandomAccessFile(m_indexPath, "rwd"); // read-write without meta sync
			m_dataFile = new RandomAccessFile(m_dataPath, "rwd"); // read-write without meta sync

			m_indexHeader.load();
			m_dataOffset = m_dataFile.length();
		}
	}

	@Override
	public Block get(MessageId id) throws IOException {
		ensureOpen(id);

		int offset = m_indexHeader.getOffset(id.getIpAddressValue(), id.getIndex());

		m_indexFile.seek(offset);

		long address = m_indexFile.readLong();
		long dataOffset = address >> 24;
		int blockOffset = (int) (address & 0xFFFFFFL);

		m_dataFile.seek(dataOffset);

		int len = m_dataFile.readInt();
		byte[] data = new byte[len];

		m_dataFile.readFully(data);

		return new DefaultBlock(id, blockOffset, data);
	}

	@Override
	public void put(Block block) throws IOException {
		Map<MessageId, Integer> ids = block.getIds();
		ByteBuf data = block.getData();

		for (Map.Entry<MessageId, Integer> e : ids.entrySet()) {
			MessageId id = e.getKey();
			int blockOffset = e.getValue();

			ensureOpen(id);

			int offset = m_indexHeader.getOffset(id.getIpAddressValue(), id.getIndex());

			m_indexFile.seek(offset);
			m_indexFile.writeLong((m_dataOffset << 24) + blockOffset);
		}

		int len = data.readableBytes();

		m_dataFile.seek(m_dataOffset);
		m_dataFile.writeInt(len);
		m_dataFile.write(data.array(), 0, len);
		m_dataOffset += len;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), m_dataPath);
	}

	private class IndexHeader {
		private static final String MAGIC_CODE = "CAT2 Local Index";

		private Map<Integer, Map<Integer, Integer>> m_blockTable = new LinkedHashMap<Integer, Map<Integer, Integer>>();

		private int m_nextBlock;

		public int getOffset(int ip, int seq) throws IOException {
			int blockIndex = seq / MESSAGE_PER_BLOCK;
			int blockOffset = (seq % MESSAGE_PER_BLOCK) * BYTE_PER_MESSAGE;
			int block = getOrCreateBlock(ip, blockIndex);
			int offset = block * BLOCK_SIZE + blockOffset;

			return offset;
		}

		private int getOrCreateBlock(int ip, int index) throws IOException {
			Map<Integer, Integer> blocks = m_blockTable.get(ip);

			if (blocks == null) {
				blocks = new HashMap<Integer, Integer>();
				m_blockTable.put(ip, blocks);
			}

			Integer block = blocks.get(index);

			if (block == null) {
				block = m_nextBlock++;
				blocks.put(index, block);
				m_indexFile.seek(m_indexOffset);
				m_indexFile.writeInt(ip);
				m_indexFile.writeInt(index);
				m_indexOffset += 8;
			}

			return block;
		}

		public void load() throws IOException {
			if (m_indexFile.length() < 2 * BLOCK_SIZE) {
				m_indexFile.seek(0);
				m_indexFile.write(MAGIC_CODE.getBytes());
				m_indexOffset = 16;
				m_nextBlock = 2;
				return; // empty or invalid header
			}

			ByteBuf buf = Unpooled.buffer(2 * BLOCK_SIZE);
			byte[] magic = new byte[16];

			m_indexFile.seek(0);
			m_indexFile.readFully(buf.array());

			buf.writerIndex(buf.capacity());
			buf.readBytes(magic);

			m_indexOffset = 16;
			m_nextBlock = 2;

			if (!new String(magic).equals(MAGIC_CODE)) {
				throw new IOException("Invalid index file: " + m_indexPath);
			}

			while (buf.isReadable()) {
				int ip = buf.readInt();
				int index = buf.readInt();

				if (ip != 0) {
					Map<Integer, Integer> blocks = m_blockTable.get(ip);

					if (blocks == null) {
						blocks = new HashMap<Integer, Integer>();
						m_blockTable.put(ip, blocks);
					}

					Integer block = blocks.get(index);

					if (block == null) {
						block = m_nextBlock++;
						blocks.put(index, block);
					}

					m_indexOffset += 8;
				} else {
					break;
				}
			}
		}
	}
}
