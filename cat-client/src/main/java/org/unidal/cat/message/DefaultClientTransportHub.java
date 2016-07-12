package org.unidal.cat.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.codec.CommandCodec;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.cat.message.command.Command;
import org.unidal.cat.transport.decode.DecodeHandler;
import org.unidal.cat.transport.decode.DecodeHandlerManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = ClientTransportHub.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultClientTransportHub implements ClientTransportHub, LogEnabled {
	@Inject(NativeCommandCodec.ID)
	private CommandCodec m_cmdCodec;

	@Inject(NativeMessageCodec.ID)
	private MessageCodec m_treeCodec;

	@Inject
	private DecodeHandlerManager m_manager;

	@Inject
	private MessageStatistics m_statistics;

	private BlockingQueue<MessageTree> m_heartbeats = new ArrayBlockingQueue<MessageTree>(60);

	private BlockingQueue<MessageTree> m_trees = new ArrayBlockingQueue<MessageTree>(1000);

	private BlockingQueue<Command> m_cmds = new ArrayBlockingQueue<Command>(10);

	private AtomicInteger m_errors = new AtomicInteger();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public boolean fill(ByteBuf buf) {
		Command cmd = m_cmds.poll();

		if (cmd != null) {
			m_cmdCodec.encode(cmd, buf);
			return true;
		}

		MessageTree heartbeat = m_heartbeats.poll();

		if (heartbeat != null) {
			m_treeCodec.encode(heartbeat, buf);
			return true;
		}

		MessageTree tree = m_trees.poll();

		if (tree != null) {
			m_treeCodec.encode(tree, buf);
			return true;
		}

		return false;
	}

	@Override
	public void onMessage(ByteBuf buf, Channel channel) {
		DecodeHandler handler = m_manager.getHandler(buf);

		handler.handle(buf);
	}

	@Override
	public void sendCommand(Command cmd) {
		m_cmds.offer(cmd);
	}

	@Override
	public void sendHeartbeat(MessageTree tree) {
		if (!m_heartbeats.offer(tree)) {
			if (m_statistics != null) {
				m_statistics.onOverflowed(null);
			}

			int count = m_errors.incrementAndGet();

			if (count % 1000 == 0 || count == 1) {
				m_logger.error("Heartbeat queue is full in tcp socket sender! Count: " + count);
			}
		}
	}

	@Override
	public void sendMessageTree(MessageTree tree) {
		if (!m_trees.offer(tree)) {
			if (m_statistics != null) {
				m_statistics.onOverflowed(null);
			}

			int count = m_errors.incrementAndGet();

			if (count % 1000 == 0 || count == 1) {
				m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
			}
		}
	}
}
