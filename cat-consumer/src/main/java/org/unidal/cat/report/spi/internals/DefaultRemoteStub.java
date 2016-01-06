package org.unidal.cat.report.spi.internals;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.unidal.cat.report.ReportConfiguration;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.cat.report.spi.remote.RemoteStub;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = RemoteStub.class)
public class DefaultRemoteStub implements RemoteStub {
	@Inject
	private ReportConfiguration m_configuration;

	@Override
	public InputStream getReport(RemoteContext ctx, String server) throws IOException {
		String url = ctx.buildURL(m_configuration.getServerUriPrefix(server));
		int timeout = m_configuration.getRemoteCallTimeoutInMillis();

		Transaction t = Cat.newTransaction("Remote", server);
		
		t.addData(url);

		try {
			Map<String, List<String>> headers = new HashMap<String, List<String>>(2);
			InputStream in = Urls.forIO().connectTimeout(timeout).readTimeout(timeout)//
			      .header("Accept-Encoding", "gzip").openStream(url, headers);

			if ("[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
				in = new GZIPInputStream(in);
			}

			t.setStatus(Message.SUCCESS);
			return in;
		} catch (IOException e) {
			Cat.logError(e);
			t.setStatus(e);
			throw e;
		} catch (RuntimeException e) {
			Cat.logError(e);
			t.setStatus(e);
			throw e;
		} catch (Error e) {
			Cat.logError(e);
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
		}
	}
}
