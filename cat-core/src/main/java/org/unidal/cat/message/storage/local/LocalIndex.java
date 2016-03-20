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

import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.FileBuilder.FileType;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Index.class, value = "local", instantiationStrategy = Named.PER_LOOKUP)
public class LocalIndex implements Index {
	private static final int BLOCK_SIZE = 32 * 1024;

	private static final int BYTE_PER_MESSAGE = 8;

	private static final int MESSAGE_PER_BLOCK = BLOCK_SIZE / BYTE_PER_MESSAGE;

	@Inject("local")
	private FileBuilder m_bulider;

	@Inject("local")
	private TokenMappingManager m_manager;

	private TokenMapping m_mapping;

	private RandomAccessFile m_file;

	private File m_path;

	private Header m_header = new Header();

	private MessageIdCodec m_codec = new MessageIdCodec();

	@Override
	public void close() {
		if (m_file != null) {
			try {
				m_header.flush();
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
	}

	private void ensureOpen(MessageId from) throws IOException {
		if (m_file == null) {
			String domain = from.getDomain();
			Date startTime = new Date(from.getTimestamp());
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

			m_path = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);
			m_path.getParentFile().mkdirs();
			m_file = new RandomAccessFile(m_path, "rwd"); // read-write without meta sync

			m_mapping = m_manager.getTokenMapping(startTime, ip);
			m_header.load();
		}
	}

	@Override
	public MessageId lookup(MessageId from) throws IOException {
		ensureOpen(from);

		int offset = m_header.getOffset(from.getIpAddressValue(), from.getIndex());
		byte[] data = new byte[8];

		m_file.seek(offset);
		m_file.read(data);

		return m_codec.decode(data, from.getHour());
	}

	@Override
	public void map(MessageId from, MessageId to) throws IOException {
		ensureOpen(from);

		int offset = m_header.getOffset(from.getIpAddressValue(), from.getIndex());
		byte[] data = m_codec.encode(to, from.getHour());

		m_file.seek(offset);
		m_file.write(data);
	}

	private class Header {
		private static final String MAGIC_CODE = "CAT2 Local Index";

		private ByteBuf m_data;

		private Map<Integer, Map<Integer, Integer>> m_blockTable = new LinkedHashMap<Integer, Map<Integer, Integer>>();

		private int m_nextBlock = 2;

		private boolean m_dirty;

		public void flush() throws IOException {
			if (m_dirty) {
				m_file.seek(0);
				m_file.write(m_data.array());
				m_file.getChannel().force(false);
			}
		}

		public int getOffset(int ip, int seq) {
			int blockIndex = seq / MESSAGE_PER_BLOCK;
			int blockOffset = (seq % MESSAGE_PER_BLOCK) * BYTE_PER_MESSAGE;
			int block = getOrCreateBlock(ip, blockIndex);
			int offset = block * BLOCK_SIZE + blockOffset;

			return offset;
		}

		private int getOrCreateBlock(int ip, int index) {
			Map<Integer, Integer> blocks = m_blockTable.get(ip);

			if (blocks == null) {
				blocks = new HashMap<Integer, Integer>();
				m_blockTable.put(ip, blocks);
			}

			Integer block = blocks.get(index);

			if (block == null) {
				block = m_nextBlock++;
				blocks.put(index, block);
				m_data.writeInt(ip);
				m_data.writeInt(index);
				m_dirty = true;
			}

			return block;
		}

		public void load() throws IOException {
			m_data = Unpooled.buffer(2 * BLOCK_SIZE);

			if (m_file.length() < m_data.capacity()) {
				m_data.writeBytes(MAGIC_CODE.getBytes());
				return; // empty or invalid header
			}

			m_file.seek(0);
			m_file.readFully(m_data.array());

			byte[] magic = new byte[16];

			m_data.writerIndex(m_data.capacity());
			m_data.readBytes(magic);

			if (!new String(magic).equals(MAGIC_CODE)) {
				throw new IOException("Invalid index file: " + m_path);
			}

			while (m_data.isReadable()) {
				int ip = m_data.readInt();
				int index = m_data.readInt();

				if (ip != 0) {
					getOrCreateBlock(ip, index);
				} else {
					break;
				}
			}

			m_data.writerIndex(m_data.readerIndex());
			m_dirty = false;
		}
	}

	class MessageIdCodec {
		public MessageId decode(byte[] data, int currentHour) throws IOException {
			int s1 = ((data[0] << 8) + data[1]) & 0XFFFF;
			int s2 = ((data[2] << 8) + data[3]) & 0XFFFF;
			int s3 = ((data[4] << 8) + data[5]) & 0XFFFF;
			int s4 = ((data[6] << 8) + data[7]) & 0XFFFF;

			String domain = m_mapping.lookup(s1);
			String ipAddressInHex = m_mapping.lookup(s2);
			int flag = (s3 >> 14) & 0x03;
			int hour = currentHour + (flag == 3 ? -1 : flag);
			int index = ((s3 & 0X3F) << 16) + s4;

			return new MessageId(domain, ipAddressInHex, hour, index);
		}

		public byte[] encode(MessageId to, int currentHour) throws IOException {
			int domainIndex = m_mapping.map(to.getDomain());
			int ipIndex = m_mapping.map(to.getIpAddressInHex());
			int hour = to.getHour() - currentHour;
			int seq = to.getIndex();
			ByteBuf buf = Unpooled.buffer(8);

			buf.writeShort(domainIndex);
			buf.writeShort(ipIndex);
			buf.writeShort((hour & 0x03) << 14 + (seq >> 16) & 0xFFFF);
			buf.writeShort(seq & 0xFFFF);
			byte[] data = buf.array();

			return data;
		}
	}
}
