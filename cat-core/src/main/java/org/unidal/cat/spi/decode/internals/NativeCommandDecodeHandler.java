package org.unidal.cat.spi.decode.internals;

import io.netty.buffer.ByteBuf;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.codec.CommandCodec;
import org.unidal.cat.codec.NativeCommandCodec;
import org.unidal.cat.message.command.Command;
import org.unidal.cat.spi.command.CommandDispatcher;
import org.unidal.cat.spi.decode.DecodeHandler;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = DecodeHandler.class, value = NativeCommandCodec.ID)
public class NativeCommandDecodeHandler implements DecodeHandler, LogEnabled {
	@Inject(NativeCommandCodec.ID)
	private CommandCodec m_codec;

	@Inject
	private CommandDispatcher m_dispatcher;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(ByteBuf buf) {
		Command cmd = null;

		try {
			cmd = m_codec.decode(buf);
			m_dispatcher.dispatch(cmd);
		} catch (Exception e) {
			m_logger.error("Error when handling " + cmd + "!", e);
		}
	}
}
