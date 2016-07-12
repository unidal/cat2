package org.unidal.cat.message.codec;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Map;

import org.unidal.cat.message.command.Command;
import org.unidal.cat.message.command.DefaultCommand;
import org.unidal.lookup.annotation.Named;

@Named(type = CommandCodec.class, value = NativeCommandCodec.ID)
public class NativeCommandCodec implements CommandCodec {
	public static final String ID = "NC1";

	@Override
	public Command decode(ByteBuf buf) {
		Context ctx = new Context(buf);
		String version = ctx.readVersion();

		if (ID.equals(version)) {
			String name = ctx.readString();
			long timestamp = ctx.readTimestamp();

			DefaultCommand cmd = new DefaultCommand(name, timestamp);
			int args = ctx.readShort();

			for (int i = 0; i < args; i++) {
				String key = ctx.readString();
				String value = ctx.readString();

				cmd.getArguments().put(key, value);
			}

			int headers = ctx.readShort();

			for (int i = 0; i < headers; i++) {
				String key = ctx.readString();
				String value = ctx.readString();

				cmd.getHeaders().put(key, value);
			}

			return cmd;
		} else {
			throw new RuntimeException(String.format("Unrecognized version(%s) for command codec!", version));
		}
	}

	@Override
	public void encode(Command cmd, ByteBuf buf) {
		Context ctx = new Context(buf);

		ctx.writeVersion(ID);
		ctx.writeString(cmd.getName());
		ctx.writeTimestamp(cmd.getTimestamp());

		Map<String, String> arguments = cmd.getArguments();
		Map<String, String> headers = cmd.getHeaders();

		ctx.writeInt(arguments.size());

		for (Map.Entry<String, String> e : arguments.entrySet()) {
			ctx.writeString(e.getKey());
			ctx.writeString(e.getValue());
		}

		ctx.writeInt(headers.size());

		for (Map.Entry<String, String> e : headers.entrySet()) {
			ctx.writeString(e.getKey());
			ctx.writeString(e.getValue());
		}
	}

	private static class Context {
		private static Charset UTF8 = Charset.forName("UTF-8");;

		private byte[] m_data = new byte[256];

		private ByteBuf m_buf;

		public Context(ByteBuf buf) {
			m_buf = buf;
		}

		public short readShort() {
			return (short) readVarint(16);
		}

		public String readVersion() {
			byte[] data = new byte[3];

			m_buf.readBytes(data);
			return new String(data);
		}

		public String readString() {
			int len = (int) readVarint(32);

			if (len == 0) {
				return "";
			} else if (len > m_data.length) {
				m_data = new byte[len];
			}

			m_buf.readBytes(m_data, 0, len);
			return new String(m_data, 0, len);
		}

		public long readTimestamp() {
			return readVarint(64);
		}

		protected long readVarint(int length) {
			int shift = 0;
			long result = 0;

			while (shift < length) {
				final byte b = m_buf.readByte();
				result |= (long) (b & 0x7F) << shift;
				if ((b & 0x80) == 0) {
					return result;
				}
				shift += 7;
			}

			throw new RuntimeException("Malformed variable int " + length + "!");
		}

		public void writeString(String str) {
			if (str == null || str.length() == 0) {
				writeVarint(0);
			} else {
				byte[] data = str.getBytes(UTF8);

				writeVarint(data.length);
				m_buf.writeBytes(data);
			}
		}

		public void writeTimestamp(long timestamp) {
			writeVarint(timestamp); // TODO use relative value of root message timestamp
		}

		public void writeInt(int value) {
			writeVarint(value); // TODO use relative value of root message timestamp
		}

		private void writeVarint(long value) {
			while (true) {
				if ((value & ~0x7FL) == 0) {
					m_buf.writeByte((byte) value);
					return;
				} else {
					m_buf.writeByte(((byte) value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		}

		public void writeVersion(String version) {
			m_buf.writeBytes(version.getBytes());
		}
	}
}
