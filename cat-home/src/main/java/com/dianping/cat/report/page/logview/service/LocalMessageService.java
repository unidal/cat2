package com.dianping.cat.report.page.logview.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class LocalMessageService extends LocalModelService<String> implements ModelService<String> {
	public static final String ID = DumpAnalyzer.ID;

	@Inject("local")
	private BucketManager m_bucketManager;

	@Inject(HtmlMessageCodec.ID)
	private MessageCodec m_html;

	@Inject(WaterfallMessageCodec.ID)
	private MessageCodec m_waterfall;

	@Inject(PlainTextMessageCodec.ID)
	private MessageCodec m_plainText;

	public LocalMessageService() {
		super("logview");
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfull = payload.isWaterfall();
		MessageId id = MessageId.parse(messageId);
		Bucket bucket = m_bucketManager.getBucket(id.getDomain(), id.getIpAddressInHex(), id.getHour(), true);
		ByteBuf data = bucket.get(id);
		MessageTree tree = null;

		if (data != null) {
			tree = m_plainText.decode(data);
		}

		if (tree != null) {
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfull) {
				m_waterfall.encode(tree, buf);
			} else {
				m_html.encode(tree, buf);
			}

			try {
				buf.readInt(); // get rid of length
				return buf.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				// ignore it
			}
		}

		return null;
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		ModelResponse<String> response = new ModelResponse<String>();
		Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());

		try {
			ModelPeriod period = request.getPeriod();
			String domain = request.getDomain();
			ApiPayload payload = new ApiPayload();

			payload.setMessageId(request.getProperty("messageId"));
			payload.setWaterfall(Boolean.valueOf(request.getProperty("waterfall", "false")));

			String report = getReport(request, period, domain, payload);

			response.setModel(report);

			t.addData("period", period);
			t.addData("domain", domain);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		if (m_manager.isHdfsOn()) {
			boolean eligibale = request.getPeriod().isCurrent();

			return eligibale;
		} else {
			return true;
		}
	}

}
