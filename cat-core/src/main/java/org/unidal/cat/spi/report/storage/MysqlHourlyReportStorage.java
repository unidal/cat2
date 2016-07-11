package org.unidal.cat.spi.report.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.cat.service.CompressionService;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;

@Named(type = ReportStorage.class, value = MysqlHourlyReportStorage.ID)
public class MysqlHourlyReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "mysql-hourly";

	@Inject
	private CompressionService m_compression;

	@Inject
	private HourlyReportDao m_hourlyDao;

	@Inject
	private HourlyReportContentDao m_hourlyContentDao;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		try {
			List<HourlyReport> hrs = m_hourlyDao.findAllByDomainNamePeriod(startTime, domain, delegate.getName(),
			      HourlyReportEntity.READSET_FULL);
			List<T> reports = new ArrayList<T>(hrs.size());

			for (HourlyReport hr : hrs) {
				try {
					HourlyReportContent content = m_hourlyContentDao.findByPK(hr.getId(),
					      HourlyReportContentEntity.READSET_FULL);
					InputStream in = new ByteArrayInputStream(content.getContent());
					InputStream cin = m_compression.decompress(in);
					T report = delegate.readStream(cin);

					reports.add(report);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			return reports;
		} catch (DalException e) {
			throw new IOException(String.format("Unable to load hourly reports(%s) from MySQL!", delegate.getName()), e);
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
			HourlyReport r = m_hourlyDao.createLocal();
			String ip = Inets.IP4.getLocalHostAddress();

			r.setName(delegate.getName());
			r.setDomain(report.getDomain());
			r.setPeriod(report.getStartTime());
			r.setIp(ip);
			r.setType(1);

			m_hourlyDao.insert(r);

			HourlyReportContent rc = m_hourlyContentDao.createLocal();

			rc.setReportId(r.getId());
			rc.setContent(out.toByteArray());

			m_hourlyContentDao.insert(rc);
		} catch (DalException e) {
			throw new IOException(String.format("Unable to store hourly report(%s) to MySQL!", delegate.getName()), e);
		}
	}
}
