package org.unidal.cat.spi.report.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;

@Named(type = ReportStorage.class, value = MysqlReportStorage.ID)
public class MysqlReportStorage<T extends Report> implements ReportStorage<T>, Initializable {
	public static final String ID = "mysql";

	@Inject
	private HourlyReportDao m_hourlyDao;

	@Inject
	private HourlyReportContentDao m_hourlyContentDao;

	@Inject
	private DailyReportDao m_dailyDao;

	@Inject
	private DailyReportContentDao m_dailyContentDao;

	private String m_ip;

	@Override
	public void initialize() throws InitializationException {
		m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		switch (period) {
		case HOUR:
			return loadAllHourly(delegate, startTime, domain);
		case DAY:
			return loadAllDaily(delegate, startTime, domain);
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private List<T> loadAllDaily(ReportDelegate<T> delegate, Date startTime, String domain) throws IOException {
		try {
			List<T> reports = new ArrayList<T>(1);
			DailyReport dr = m_dailyDao.findByDomainNamePeriod(domain, delegate.getName(), startTime,
			      DailyReportEntity.READSET_FULL);
			DailyReportContent content = m_dailyContentDao.findByPK(dr.getId(), DailyReportContentEntity.READSET_FULL);
			T report = delegate.parseBinary(content.getContent());

			reports.add(report);
			return reports;
		} catch (DalNotFoundException e) {
			return Collections.emptyList();
		} catch (DalException e) {
			throw new IOException(String.format("Unable to load daily reports(%s) from MySQL!", delegate.getName()), e);
		}
	}

	private List<T> loadAllHourly(ReportDelegate<T> delegate, Date startTime, String domain) throws IOException {
		try {
			List<HourlyReport> hrs = m_hourlyDao.findAllByDomainNamePeriod(startTime, domain, delegate.getName(),
			      HourlyReportEntity.READSET_FULL);
			List<T> reports = new ArrayList<T>(hrs.size());

			for (HourlyReport hr : hrs) {
				try {
					HourlyReportContent content = m_hourlyContentDao.findByPK(hr.getId(),
					      HourlyReportContentEntity.READSET_FULL);
					T report = delegate.parseBinary(content.getContent());

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
		if (!policy.forMySQL()) {
			return;
		}

		switch (period) {
		case HOUR:
			storeHourly(delegate, report);
			break;
		case DAY:
			storeDaily(delegate, report);
			break;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private void storeDaily(ReportDelegate<T> delegate, T report) throws IOException {
		byte[] content = delegate.buildBinary(report);

		try {
			DailyReport r = m_dailyDao.createLocal();

			r.setName(delegate.getName());
			r.setDomain(report.getDomain());
			r.setPeriod(report.getStartTime());
			r.setIp(m_ip);
			r.setType(1);

			m_dailyDao.insert(r);

			DailyReportContent rc = m_dailyContentDao.createLocal();

			rc.setReportId(r.getId());
			rc.setContent(content);
			m_dailyContentDao.insert(rc);
		} catch (DalException e) {
			throw new IOException(String.format("Unable to store daily report(%s) to MySQL!", delegate.getName()), e);
		}
	}

	private void storeHourly(ReportDelegate<T> delegate, T report) throws IOException {
		byte[] binaryContent = delegate.buildBinary(report);

		try {
			HourlyReport r = m_hourlyDao.createLocal();
			HourlyReportContent rc = m_hourlyContentDao.createLocal();

			r.setName(delegate.getName());
			r.setDomain(report.getDomain());
			r.setPeriod(report.getStartTime());
			r.setIp(m_ip);
			r.setType(1);

			m_hourlyDao.insert(r);

			rc.setReportId(r.getId());
			rc.setContent(binaryContent);
			m_hourlyContentDao.insert(rc);
		} catch (DalException e) {
			throw new IOException(String.format("Unable to store hourly report(%s) to MySQL!", delegate.getName()), e);
		}
	}
}
