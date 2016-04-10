package org.unidal.cat.message.codec;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageCodec.class, value = BinaryMessageCodec.ID)
public class BinaryMessageCodec implements MessageCodec {
	public static final String ID = "binary";

	private static final String VERSION = "BT1"; // binary log tree version 1

	@Override
	public MessageTree decode(ByteBuf buf) {
		MessageTree tree = new DefaultMessageTree();

		decode(buf, tree);
		return tree;
	}

	@Override
	public void decode(ByteBuf buf, MessageTree tree) {
		Context ctx = new Context(tree);

		Codec.HEADER.decode(ctx, buf);

		Message msg = decodeMessage(ctx, buf);

		tree.setMessage(msg);
	}

	private Message decodeMessage(Context ctx, ByteBuf buf) {
		Message msg = null;

		while (buf.readableBytes() > 0) {
			char ch = ctx.readId(buf);

			switch (ch) {
			case 't':
				Codec.TRANSACTION_START.decode(ctx, buf);
				break;
			case 'T':
				msg = Codec.TRANSACTION_END.decode(ctx, buf);
				break;
			case 'E':
				Message e = Codec.EVENT.decode(ctx, buf);

				ctx.addChild(e);
				break;
			case 'H':
				Message h = Codec.HEARTBEAT.decode(ctx, buf);

				ctx.addChild(h);
				break;
			default:
				throw new RuntimeException(String.format("Unsupported message type(%s).", ch));
			}
		}

		return msg;
	}

	@Override
	public void encode(MessageTree tree, ByteBuf buf) {
		Message msg = tree.getMessage();
		Context ctx = new Context(tree);

		Codec.HEADER.encode(ctx, buf, null);

		if (msg != null) {
			encodeMessage(ctx, buf, msg);
		}
	}

	private void encodeMessage(Context ctx, ByteBuf buf, Message msg) {
		if (msg instanceof Transaction) {
			Transaction transaction = (Transaction) msg;
			List<Message> children = transaction.getChildren();

			Codec.TRANSACTION_START.encode(ctx, buf, msg);

			for (Message child : children) {
				if (child != null) {
					encodeMessage(ctx, buf, child);
				}
			}

			Codec.TRANSACTION_END.encode(ctx, buf, msg);
		} else if (msg instanceof Event) {
			Codec.EVENT.encode(ctx, buf, msg);
		} else if (msg instanceof Heartbeat) {
			Codec.HEARTBEAT.encode(ctx, buf, msg);
		} else {
			throw new RuntimeException(String.format("Unsupported message(%s).", msg));
		}
	}

	static enum Codec {
		HEADER {
			@Override
			protected Message decode(Context ctx, ByteBuf buf) {
				MessageTree tree = ctx.getMessageTree();
				String version = ctx.getVersion(buf);

				if (VERSION.equals(version)) {
					tree.setDomain(ctx.readString(buf));
					tree.setHostName(ctx.readString(buf));
					tree.setIpAddress(ctx.readString(buf));
					tree.setThreadGroupName(ctx.readString(buf));
					tree.setThreadId(ctx.readString(buf));
					tree.setThreadName(ctx.readString(buf));
					tree.setMessageId(ctx.readString(buf));
					tree.setParentMessageId(ctx.readString(buf));
					tree.setRootMessageId(ctx.readString(buf));
					tree.setSessionToken(ctx.readString(buf));
				} else {
					throw new RuntimeException(String.format("Unrecognized version(%s) for binary message codec!", version));
				}

				return null;
			}

			@Override
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				MessageTree tree = ctx.getMessageTree();

				ctx.writeVersion(buf, "BT1");
				ctx.writeString(buf, tree.getDomain());
				ctx.writeString(buf, tree.getHostName());
				ctx.writeString(buf, tree.getIpAddress());
				ctx.writeString(buf, tree.getThreadGroupName());
				ctx.writeString(buf, tree.getThreadId());
				ctx.writeString(buf, tree.getThreadName());
				ctx.writeString(buf, tree.getMessageId());
				ctx.writeString(buf, tree.getParentMessageId());
				ctx.writeString(buf, tree.getRootMessageId());
				ctx.writeString(buf, tree.getSessionToken());
			}
		},

		TRANSACTION_START {
			@Override
			protected Message decode(Context ctx, ByteBuf buf) {
				long timestamp = ctx.readTimestamp(buf);
				String type = ctx.readString(buf);
				String name = ctx.readString(buf);
				DefaultTransaction t = new DefaultTransaction(type, name, null);

				t.setTimestamp(timestamp);
				ctx.pushTransaction(t);
				return t;
			}

			@Override
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 't');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
			}
		},

		TRANSACTION_END {
			@Override
			protected Message decode(Context ctx, ByteBuf buf) {
				String status = ctx.readString(buf);
				String data = ctx.readString(buf);
				long durationInMicros = ctx.readDuration(buf);
				DefaultTransaction t = ctx.popTransaction();

				t.setStatus(status);
				t.addData(data);
				t.setDurationInMicros(durationInMicros);
				return t;
			}

			@Override
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				Transaction t = (Transaction) msg;

				ctx.writeId(buf, 'T');
				ctx.writeString(buf, msg.getStatus());
				ctx.writeString(buf, msg.getData().toString());
				ctx.writeDuration(buf, t.getDurationInMicros());
			}
		},

		EVENT {
			@Override
			protected Message decode(Context ctx, ByteBuf buf) {
				long timestamp = ctx.readTimestamp(buf);
				String type = ctx.readString(buf);
				String name = ctx.readString(buf);
				String status = ctx.readString(buf);
				String data = ctx.readString(buf);
				DefaultEvent e = new DefaultEvent(type, name);

				e.setTimestamp(timestamp);
				e.setStatus(status);
				e.addData(data);
				return e;
			}

			@Override
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 'E');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
				ctx.writeString(buf, msg.getStatus());
				ctx.writeString(buf, msg.getData().toString());
			}
		},

		HEARTBEAT {
			@Override
			protected Message decode(Context ctx, ByteBuf buf) {
				long timestamp = ctx.readTimestamp(buf);
				String type = ctx.readString(buf);
				String name = ctx.readString(buf);
				String status = ctx.readString(buf);
				String data = ctx.readString(buf);
				DefaultHeartbeat h = new DefaultHeartbeat(type, name);

				h.setTimestamp(timestamp);
				h.setStatus(status);
				h.addData(data);
				return h;
			}

			@Override
			protected void encode(Context ctx, ByteBuf buf, Message msg) {
				ctx.writeId(buf, 'H');
				ctx.writeTimestamp(buf, msg.getTimestamp());
				ctx.writeString(buf, msg.getType());
				ctx.writeString(buf, msg.getName());
				ctx.writeString(buf, msg.getStatus());
				ctx.writeString(buf, msg.getData().toString());
			}
		};

		protected abstract Message decode(Context ctx, ByteBuf buf);

		protected abstract void encode(Context ctx, ByteBuf buf, Message msg);
	}

	private static class Context {
		private static Charset UTF8 = Charset.forName("UTF-8");;

		private MessageTree m_tree;

		private Stack<DefaultTransaction> m_parents = new Stack<DefaultTransaction>();

		private byte[] m_data = new byte[256];

		public Context(MessageTree tree) {
			m_tree = tree;
		}

		public void addChild(Message msg) {
			if (!m_parents.isEmpty()) {
				m_parents.peek().addChild(msg);
			}
		}

		public MessageTree getMessageTree() {
			return m_tree;
		}

		public String getVersion(ByteBuf buf) {
			byte[] data = new byte[3];

			buf.readBytes(data);
			return new String(data);
		}

		public DefaultTransaction popTransaction() {
			return m_parents.pop();
		}

		public void pushTransaction(DefaultTransaction t) {
			if (!m_parents.isEmpty()) {
				m_parents.peek().addChild(t);
			}

			m_parents.push(t);
		}

		public long readDuration(ByteBuf buf) {
			return readVarint(buf, 32);
		}

		public char readId(ByteBuf buf) {
			return (char) buf.readByte();
		}

		public String readString(ByteBuf buf) {
			int len = (int) readVarint(buf, 32);

			if (len == 0) {
				return "";
			} else if (len > m_data.length) {
				m_data = new byte[len];
			}

			buf.readBytes(m_data, 0, len);
			return new String(m_data, 0, len);
		}

		public long readTimestamp(ByteBuf buf) {
			return readVarint(buf, 64);
		}

		protected long readVarint(ByteBuf buf, int length) {
			int shift = 0;
			long result = 0;

			while (shift < length) {
				final byte b = buf.readByte();
				result |= (long) (b & 0x7F) << shift;
				if ((b & 0x80) == 0) {
					return result;
				}
				shift += 7;
			}

			throw new RuntimeException("Malformed variable int " + length + "!");
		}

		public void writeDuration(ByteBuf buf, long duration) {
			writeVarint(buf, duration);
		}

		public void writeId(ByteBuf buf, char id) {
			buf.writeByte(id);
		}

		public void writeString(ByteBuf buf, String str) {
			if (str == null || str.length() == 0) {
				writeVarint(buf, 0);
			} else {
				byte[] data = str.getBytes(UTF8);

				writeVarint(buf, data.length);
				buf.writeBytes(data);
			}
		}

		public void writeTimestamp(ByteBuf buf, long timestamp) {
			writeVarint(buf, timestamp); // TODO use relative value of root message timestamp
		}

		private void writeVarint(ByteBuf buf, long value) {
			while (true) {
				if ((value & ~0x7FL) == 0) {
					buf.writeByte((byte) value);
					return;
				} else {
					buf.writeByte(((byte) value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		}

		public void writeVersion(ByteBuf buf, String version) {
			buf.writeBytes(version.getBytes());
		}
	}
}
