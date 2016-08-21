package org.unidal.cat.spi.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.unidal.cat.spi.report.ReportConfiguration;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;

@Named(type = RemoteStub.class)
public class DefaultRemoteStub implements RemoteStub {
	@Inject
	private ReportConfiguration m_configuration;

	@Override
	public InputStream getReport(RemoteContext ctx, String server) throws IOException {
		DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
		Transaction t = cat.newTransaction(ctx.getParentTransaction(), "Remote", server);

		try {
			String url = ctx.buildURL(m_configuration.getServerUriPrefix(server));
			int ct = m_configuration.getRemoteCallConnectTimeoutInMillis();
			int rt = m_configuration.getRemoteCallReadTimeoutInMillis();

			t.addData(url);
			Map<String, List<String>> headers = new HashMap<String, List<String>>(2);
			InputStream in = Urls.forIO().connectTimeout(ct).readTimeout(rt)//
			      .header("Accept-Encoding", "gzip").openStream(url, headers);

			if ("[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
				in = new GZIPInputStream(in);
			}

			t.setStatus(Message.SUCCESS);
			return in;
		} catch (IOException e) {
			t.setStatus(e);
			throw e;
		} catch (RuntimeException e) {
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}
}
