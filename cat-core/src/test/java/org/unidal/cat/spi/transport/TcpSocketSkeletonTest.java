package org.unidal.cat.spi.transport;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.unidal.cat.transport.TcpSocketStub;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.net.SocketAddressProvider;

import com.dianping.cat.message.internal.MessageId;

public class TcpSocketSkeletonTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		defineComponent(SocketAddressProvider.class, MockSocketAddressProvider.class);

		TcpSocketSkeleton skeleton = lookup(TcpSocketSkeleton.class);
		TcpSocketStub stub = lookup(TcpSocketStub.class);

		Threads.forGroup("Cat").start(skeleton);
		Threads.forGroup("Cat").start(stub);

		long hour = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

		stub.getHub().sendMessageTree(TreeHelper.tree(new MessageId("Test", "7f000001", (int) hour, 0)));

		System.in.read();
	}

	public static class MockSocketAddressProvider implements SocketAddressProvider {
		@Override
		public List<InetSocketAddress> getAddresses() {
			return Arrays.asList(new InetSocketAddress("127.0.0.1", 2280));
		}
	}
}
