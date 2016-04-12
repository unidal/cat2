package com.dianping.cat.analysis;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.spi.analysis.MessageDispatcher;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageHandler extends ContainerHolder implements MessageHandler, LogEnabled {
	@Inject
	private MessageConsumer m_consumer;

	@Inject
	private MessageDispatcher m_dispatcher;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void handle(MessageTree tree) {
		if (m_consumer == null) {
			m_consumer = lookup(MessageConsumer.class);
		}

		if (m_dispatcher == null) {
			m_dispatcher = lookup(MessageDispatcher.class);
		}

		try {
			m_consumer.consume(tree);
			m_dispatcher.dispatch(tree);
		} catch (Throwable e) {
			m_logger.error("Error when consuming message in " + m_consumer + "! tree: " + tree, e);
		}
	}
}
