package org.unidal.cat.spi.transport;

import io.netty.buffer.ByteBuf;

import org.junit.Test;
import org.unidal.cat.transport.decode.DecodeHandler;
import org.unidal.cat.transport.decode.DecodeHandlerManager;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.TcpSocketReceiver;

public class TcpSocketReceiverTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		defineComponent(ServerTransportConfiguration.class, MockTransportConfiguration.class);
		defineComponent(DecodeHandlerManager.class, MockCodecHandlerManager.class);

		TcpSocketReceiver receiver = lookup(TcpSocketReceiver.class);

		receiver.setup();
		receiver.destory();
	}

	private static class MockCodecHandler implements DecodeHandler {
		@Override
		public void handle(ByteBuf buf) {
		}
	}

	public static final class MockCodecHandlerManager implements DecodeHandlerManager {
		private MockCodecHandler m_handler = new MockCodecHandler();

		@Override
		public DecodeHandler getHandler(ByteBuf buf) {
			return m_handler;
		}
	}

	public static final class MockTransportConfiguration implements ServerTransportConfiguration {
		@Override
		public int getBossThreads() {
			return 1;
		}

		@Override
		public int getTcpPort() {
			return 2282;
		}

		@Override
		public int getWorkerThreads() {
			return 4;
		}
	}
}
