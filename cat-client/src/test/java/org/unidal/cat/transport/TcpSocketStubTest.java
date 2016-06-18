package org.unidal.cat.transport;

import java.io.IOException;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

public class TcpSocketStubTest extends ComponentTestCase {
	@Test
	public void test() throws IOException {
		TcpSocketStub stub = lookup(TcpSocketStub.class);

		Threads.forGroup("Cat").start(stub);
		
		System.in.read();
		
		stub.shutdown();
	}
}
