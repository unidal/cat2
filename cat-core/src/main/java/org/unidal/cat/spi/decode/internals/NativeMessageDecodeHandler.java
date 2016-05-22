package org.unidal.cat.spi.decode.internals;

import io.netty.buffer.ByteBuf;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.cat.spi.analysis.MessageDispatcher;
import org.unidal.cat.spi.decode.DecodeHandler;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = DecodeHandler.class, value = NativeMessageCodec.ID)
public class NativeMessageDecodeHandler implements DecodeHandler, LogEnabled {
	@Inject(NativeMessageCodec.ID)
	private MessageCodec m_codec;

	@Inject
	private MessageConsumer m_consumer;

	@Inject
	private MessageDispatcher m_dispatcher;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private volatile long m_processCount;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(ByteBuf buf) {
		try {
			MessageTree tree = m_codec.decode(buf);

			// TODO remove m_consumer after all analyzers migrated
			m_consumer.consume(tree);
			m_dispatcher.dispatch(tree);

			m_processCount++;

			long flag = m_processCount % CatConstants.SUCCESS_COUNT;

			if (flag == 0) {
				m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
			}
		} catch (Exception e) {
			m_serverStateManager.addMessageTotalLoss(1);
			m_logger.error(e.getMessage(), e);
		}
	}
}
