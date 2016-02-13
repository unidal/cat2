package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Block;

public class DefaultBlock implements Block {
	private static final int MAX_SIZE = 64 * 1024;

	private String m_domain;

	private int m_hour;

	private ByteBuf m_data;

	private int m_offset;

	private Map<MessageId, Integer> m_ids = new LinkedHashMap<MessageId, Integer>();

	private DataOutputStream m_out;

	public DefaultBlock(MessageId id, int offset, byte[] data) {
		m_ids.put(id, offset);
		m_data = Unpooled.wrappedBuffer(data);
	}

	public DefaultBlock(String domain, int hour) {
		m_domain = domain;
		m_hour = hour;
		m_data = Unpooled.buffer(8 * 1024);
		m_data.setZero(0, m_data.capacity());

		DeflaterOutputStream os = new DeflaterOutputStream(new ByteBufOutputStream(m_data), new Deflater(5, true), 512);
		m_out = new DataOutputStream(os);
	}

	@Override
	public void finish() {
		if (m_out != null) {
			try {
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
	public Map<MessageId, Integer> getIds() {
		return m_ids;
	}

	@Override
	public boolean isFull() {
		return m_offset >= MAX_SIZE;
	}

	@Override
	public void pack(MessageId id, ByteBuf buf) throws IOException {
		int len = buf.readableBytes();

		if (id.getIndex() == 134) {
			System.out.println("len = " + len);
			System.out.println(new String(buf.array(), 0 , len));
		}
		
		m_out.writeInt(len);
		buf.readBytes(m_out, len);

		m_ids.put(id, m_offset);
		m_offset += len + 4;
	}

	@Override
	public ByteBuf unpack(MessageId id) throws IOException {
		Inflater inflater = new Inflater(true);
		InflaterInputStream is = new InflaterInputStream(new ByteBufInputStream(m_data), inflater, 512);
		DataInputStream in = new DataInputStream(is);
		int offset = m_ids.get(id);

		in.skip(offset);

		int len = in.readInt();
		byte[] data = new byte[len];
		
		in.readFully(data);
		in.close();
		
		if (id.getIndex() == 134) {
			System.out.println("len : " + len);
			System.out.println(new String(data, 0 , len));
		}

		return Unpooled.wrappedBuffer(data);
	}
}
