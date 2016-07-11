package org.unidal.cat.spi.report.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.unidal.cat.service.CompressionService;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;

@Named(type = ReportStorage.class, value = MysqlHistoryReportStorage.ID)
public class MysqlHistoryReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "mysql-history";

	@Inject
	private CompressionService m_compression;

	@Inject
	private DailyReportDao m_dailyDao;

	@Inject
	private DailyReportContentDao m_dailyContentDao;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		try {
			List<T> reports = new ArrayList<T>(1);
			DailyReport dr = m_dailyDao.findByDomainNamePeriod(domain, delegate.getName(), startTime,
			      DailyReportEntity.READSET_FULL);
			DailyReportContent content = m_dailyContentDao.findByPK(dr.getId(), DailyReportContentEntity.READSET_FULL);
			InputStream in = new ByteArrayInputStream(content.getContent());
			InputStream cin = m_compression.decompress(in);
			T report = delegate.readStream(cin);

			reports.add(report);
			return reports;
		} catch (DalNotFoundException e) {
			return Collections.emptyList();
		} catch (DalException e) {
			throw new IOException(String.format("Unable to load %s reports(%s) from MySQL!", period.getName(),
			      delegate.getName()), e);
		}
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(2 * 1024 * 1024);
		OutputStream cout = m_compression.compress(out);

		delegate.writeStream(cout, report);
		cout.close();

		try {
			DailyReport r = m_dailyDao.createLocal();
			String ip = Inets.IP4.getLocalHostAddress();

			r.setType(period.getId());
			r.setName(delegate.getName());
			r.setDomain(report.getDomain());
			r.setPeriod(report.getStartTime());
			r.setIp(ip);
			r.setType(1);

			m_dailyDao.insert(r);

			DailyReportContent rc = m_dailyContentDao.createLocal();

			rc.setReportId(r.getId());
			rc.setContent(out.toByteArray());
			m_dailyContentDao.insert(rc);
		} catch (DalException e) {
			throw new IOException(String.format("Unable to store %s report(%s) to MySQL!", period.getName(),
			      delegate.getName()), e);
		}
	}
}
