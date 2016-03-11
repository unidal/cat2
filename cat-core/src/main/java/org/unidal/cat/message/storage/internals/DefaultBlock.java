package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Block;

public class DefaultBlock implements Block {
	private static final int MAX_SIZE = 256 * 1024;

	private static final int BUFFER_SIZE = 1024;

	private String m_domain;

	private int m_hour;

	private ByteBuf m_data;

	private int m_offset;

	private Map<MessageId, Integer> m_mappings = new LinkedHashMap<MessageId, Integer>();

	private DeflaterOutputStream m_out;

	private boolean m_gzip = true;

	public DefaultBlock(MessageId id, int offset, byte[] data) {
		m_mappings.put(id, offset);
		m_data = data == null ? null : Unpooled.wrappedBuffer(data);
	}

	public DefaultBlock(String domain, int hour) {
		m_domain = domain;
		m_hour = hour;
		m_data = Unpooled.buffer(8 * 1024);

		ByteBufOutputStream os = new ByteBufOutputStream(m_data);

		if (m_gzip) {
			try {
				m_out = new GZIPOutputStream(os, BUFFER_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			m_out = new DeflaterOutputStream(os, new Deflater(5, true), BUFFER_SIZE);
		}
	}

	@Override
	public void finish() {
		if (m_out != null) {
			try {
				m_out.finish();
				m_out.flush();
				m_out.close();
			} catch (IOException e) {
				// ignore it
			}

			m_out = null;
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
	public Map<MessageId, Integer> getMappings() {
		return m_mappings;
	}

	@Override
	public boolean isFull() {
		return m_offset >= MAX_SIZE;
	}

	@Override
	public void pack(MessageId id, ByteBuf buf) throws IOException {
		int len = buf.readableBytes();

		buf.readBytes(m_out, len);
		m_mappings.put(id, m_offset);
		m_offset += len;
	}

	@Override
	public ByteBuf unpack(MessageId id) throws IOException {
		if (m_data == null) {
			return null;
		}

		ByteBufInputStream is = new ByteBufInputStream(m_data);
		DataInputStream in;

		if (m_gzip) {
			in = new DataInputStream(new GZIPInputStream(is, BUFFER_SIZE));
		} else {
			Inflater inflater = new Inflater(true);

			in = new DataInputStream(new InflaterInputStream(is, inflater, BUFFER_SIZE));
		}

		int offset = m_mappings.get(id);

		in.skip(offset);

		int len = in.readInt();
		byte[] data = new byte[len];

		in.readFully(data);
		in.close();

		ByteBuf buf = Unpooled.wrappedBuffer(data);

		return buf;
	}
}
