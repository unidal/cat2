package org.unidal.cat.spi.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.unidal.cat.message.codec.CommandCodec;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.command.Command;
import org.unidal.cat.spi.transport.ServerTransportHub;
import org.unidal.cat.transport.decode.DecodeHandler;
import org.unidal.cat.transport.decode.DecodeHandlerManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ServerTransportHub.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultServerTransportHub implements ServerTransportHub {
	@Inject(NativeCommandCodec.ID)
	private CommandCodec m_cmdCodec;

	@Inject
	private DecodeHandlerManager m_manager;

	private BlockingQueue<Command> m_cmds = new ArrayBlockingQueue<Command>(10);

	@Override
	public boolean fill(ByteBuf buf) {
		Command cmd = m_cmds.poll();

		if (cmd != null) {
			m_cmdCodec.encode(cmd, buf);
			return true;
		}

		return false;
	}

	@Override
	public void sendCommand(Command cmd) {
		m_cmds.offer(cmd);
	}

	@Override
	public void onMessage(ByteBuf buf, Channel channel) {
		DecodeHandler handler = m_manager.getHandler(buf);

		handler.handle(buf);
	}

}
