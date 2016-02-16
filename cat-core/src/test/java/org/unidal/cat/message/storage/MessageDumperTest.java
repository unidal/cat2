package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.MessageId;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MessageDumperTest extends ComponentTestCase {
	private TreeHelper m_helper = new TreeHelper();

	@Before
	public void before() {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));
	}

	@Test
	public void testRead() throws Exception {
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket("mock", 404259, true);

		for (int i = 0; i < 100000; i++) {
			MessageId id = MessageId.parse("mock-0a010203-404259-" + i);

			try {
				Block block = bucket.get(id);
				ByteBuf buf = block.unpack(id);
				MessageTree tree = m_helper.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			} catch (Exception e) {
				throw new Exception(String.format("Error when loading message(%s)! ", id), e);
			}
		}
	}

	@Test
	public void testWrite() throws Exception {
		MessageDumper dumper = lookup(MessageDumper.class);

		for (int i = 0; i < 100000; i++) {
			String id = "mock-0a010203-404259-" + i;
			DefaultMessageTree tree = m_helper.tree(id, i);

			m_helper.encode(tree);
			dumper.process(tree);
		}

		dumper.awaitTermination();
	}

	class TreeHelper {
		public MessageTree decode(ByteBuf buf) {
			// buf.readInt(); // get rid of it

			MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
			MessageTree tree = buf == null ? null : codec.decode(buf);

			return tree;
		}

		public void encode(DefaultMessageTree tree) {
			MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
			ByteBuf buf = Unpooled.buffer(2048);

			codec.encode(tree, buf);
			tree.setBuffer(buf);
		}

		DefaultMessageTree tree(String messageId, final int index) {
			Message message = new MockMessageBuilder() {
				@Override
				public MessageHolder define() {
					TransactionHolder t = t("WEBCLUSTER", "GET",
					      "This&123123&1231&3&\n\n\n\n&\t\t\t\n\n\n\n\n\n is test data\t\t\n\n", 112819) //
					      .at(1455333904000L + index) //
					      .after(1300).child(t("QUICKIESERVICE", "gimme_stuff", 1571)) //
					      .after(100).child(e("SERVICE", "event1", "This\n\n\n\n\n\n is test data\t\t\n\n")) //
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

			DefaultMessageTree tree = new DefaultMessageTree();

			tree.setDomain("mock");
			tree.setHostName("mock-host");
			tree.setIpAddress("10.1.2.3");
			tree.setThreadGroupName("test");
			tree.setThreadId("test");
			tree.setThreadName("test");
			tree.setMessage(message);
			tree.setMessageId(messageId);
			return tree;
		}
	}
}
