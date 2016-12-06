package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TcpSocketSenderTest extends ComponentTestCase {

	public static Date getCurrentHour(int index) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, index);

		return cal.getTime();
	}

	public MessageTree buildAtomicMessage(final int total) {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("SQL", "GET" + total, 112819) //
				      .at(System.currentTimeMillis());

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
				      .at(System.currentTimeMillis()) //
				      .after(1300).child(t("QUICKIE SERVICE", "gimme_stuff", 1571)) //
				      .after(100).child(e("SERVICE", "event1")) //
				      .after(100).child(h("SERVICE", "heartbeat1")) //
				      .after(100).child(t("WEB SERVER", "GET", 109358) //
				            .after(1000).child(t("SOME SERVICE", "get", 4345) //
				                  .after(4000).child(t("MEMCACHED", "Get", 279))) //
				            .mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
				            .reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
				                  .after(1000).mark().child(t("SERVICE", "getStuff", 3760)) //
				                  .reset().child(t("DATAR", "findThings", 94537)) //
				                  .after(200).child(t("THINGIE", "getMoar", 1435)) //
				            ) //
				            .after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 378)) //
				                  .reset().child(t("MEMCACHED", "Get", 3496)) //
				            ) //
				            .reset().child(t("FINAL DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				            ) //
				      ) //
				;

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

	public MessageTree buildSimpleMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
				      .at(System.currentTimeMillis()) //
				;

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

	@Test
	public void test() throws InterruptedException {
		TcpSocketSender sender = lookup(TcpSocketSender.class);
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

		servers.add(new InetSocketAddress("cat01.beta", 2280));
		servers.add(new InetSocketAddress("cat02.beta", 2280));
		sender.initialize(servers);

		Thread.sleep(1000);

		long start = System.currentTimeMillis();
		long total = 60000000000l;
		final MessageTree message = buildMessage();

		System.err.println(PlainTextMessageCodec.encodeTree(message));

		for (int i = 0; i < total; i++) {
			DefaultTransaction t = (DefaultTransaction) message.getMessage();

			if (t != null) {
				t.setTimestamp(System.currentTimeMillis());
				sender.sendMessageForTest(message);
			} else {
				System.out.println(i);
			}

		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:" + total * 1000.0 / duration);
	}

	@Test
	public void testCodec() {
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		long start = System.currentTimeMillis();
		int total = 1000000;
		MessageTree msg = buildMessage();

		for (int i = 0; i < total; i++) {

			ByteBuf buf = codec.encode(msg);

			buf.release();
		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:" + total * 1000.0 / duration);
	}

	@Test
	public void testMergeAtomicTree() throws InterruptedException {
		TcpSocketSender sender = lookup(TcpSocketSender.class);
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

		servers.add(new InetSocketAddress("cat01.beta", 2280));
		servers.add(new InetSocketAddress("cat02.beta", 2280));
		sender.initialize(servers);

		Thread.sleep(1000);

		long start = System.currentTimeMillis();
		int total = 1000;

		for (int i = 0; i < total; i++) {
			MessageTree message = buildAtomicMessage(i);
			DefaultTransaction t = (DefaultTransaction) message.getMessage();

			if (t != null) {
				t.setName("name:" + i);

				if (i < 150) {
					t.setTimestamp(getCurrentHour(-1).getTime() + i * 100);
				}
			}
			sender.send(message);
		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:" + total * 1000.0 / duration);

		Thread.sleep(10000);
	}

	@Test
	public void testServerNotWork() {
		Cat.initialize("127.0.0.1");

		while (true) {
			Transaction t = Cat.newTransaction("test", "test");

			((DefaultTransaction) t).setTimestamp(System.currentTimeMillis() - 60 * 60 * 1000);

			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			t.complete();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testSimple() throws InterruptedException {
		TcpSocketSender sender = lookup(TcpSocketSender.class);
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

		servers.add(new InetSocketAddress("cat01.beta", 2280));
		servers.add(new InetSocketAddress("cat02.beta", 2280));
		sender.initialize(servers);

		Thread.sleep(1000);

		long start = System.currentTimeMillis();
		long total = 60000000000l;
		final MessageTree message = buildSimpleMessage();

		System.err.println(message);

		for (int i = 0; i < total; i++) {
			DefaultTransaction t = (DefaultTransaction) message.getMessage();

			if (t != null) {
				t.setTimestamp(System.currentTimeMillis());
				sender.sendMessageForTest(message);
			} else {
				System.out.println(i);
			}

		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:" + total * 1000.0 / duration);
	}

	@Test
	public void testSimpleCodec() {
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		long start = System.currentTimeMillis();
		int total = 1000000;
		MessageTree msg = buildSimpleMessage();

		for (int i = 0; i < total; i++) {
			ByteBuf buf = codec.encode(msg);

			System.out.println(buf.readableBytes());
			buf.release();
		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:" + total * 1000.0 / duration);
	}
}
