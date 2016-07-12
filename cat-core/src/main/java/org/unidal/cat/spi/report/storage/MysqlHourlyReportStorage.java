package org.unidal.cat.spi.report.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.cat.dal.report.HourlyReportContentDao;
import org.unidal.cat.dal.report.HourlyReportContentDo;
import org.unidal.cat.dal.report.HourlyReportContentEntity;
import org.unidal.cat.dal.report.HourlyReportDao;
import org.unidal.cat.dal.report.HourlyReportDo;
import org.unidal.cat.dal.report.HourlyReportEntity;
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

@Named(type = ReportStorage.class, value = MysqlHourlyReportStorage.ID)
public class MysqlHourlyReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "mysql-hourly";

	@Inject
	private CompressionService m_compression;

	@Inject
	private HourlyReportDao m_dao;

	@Inject
	private HourlyReportContentDao m_contentDao;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		try {
			List<HourlyReportDo> hrs = m_dao.findAllByDomainAndNameAndStartTime(domain, delegate.getName(), startTime,
			      HourlyReportEntity.READSET_FULL);
			List<T> reports = new ArrayList<T>(hrs.size());

			for (HourlyReportDo hr : hrs) {
				try {
					HourlyReportContentDo content = m_contentDao
					      .findByPK(hr.getId(), HourlyReportContentEntity.READSET_FULL);
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
			HourlyReportDo r = m_dao.createLocal();
			String ip = Inets.IP4.getLocalHostAddress();

			r.setName(delegate.getName());
			r.setDomain(report.getDomain());
			r.setStartTime(report.getStartTime());
			r.setIp(ip);
			r.setIndex(index);

			m_dao.insert(r);

			HourlyReportContentDo rc = m_contentDao.createLocal();

			rc.setReportId(r.getId());
			rc.setFormat(11);
			rc.setContent(out.toByteArray());

			m_contentDao.insert(rc);
		} catch (DalException e) {
			throw new IOException(String.format("Unable to store hourly report(%s) to MySQL!", delegate.getName()), e);
		}
	}
}
