package org.unidal.cat.core.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.spi.Report;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.service.ProjectService.Department;

public abstract class CoreReportModel<P extends Page, A extends Action, M extends ActionContext<?>> extends
      ViewModel<P, A, M> {
	private transient String m_id;

	private transient GroupBar m_groupBar;

	// --- old stuff ---

	private transient Throwable m_exception;

	private transient ProjectService m_projectService;

	private transient HostinfoService m_hostinfoService;

	public CoreReportModel(String id, M ctx) {
		super(ctx);

		m_id = id;

		try {
			m_projectService = ContainerLoader.getDefaultContainer().lookup(ProjectService.class);
			m_hostinfoService = ContainerLoader.getDefaultContainer().lookup(HostinfoService.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	public abstract String getDomain();

	public Map<String, Department> getDomainGroups() {
		return m_projectService.findDepartments(getDomains());
	}

	public abstract Collection<String> getDomains();

	/* used by report-navbar.tag */
	public abstract Report getReport();

	public Throwable getException() {
		return m_exception;
	}

	/* used by report-navbar.tag */
	public GroupBar getGroupBar() {
		return m_groupBar;
	}

	public String getId() {
		return m_id;
	}

	public List<String> getIps() {
		return new ArrayList<String>();
	}

	public Map<String, String> getIpToHostname() {
		List<String> ips = getIps();
		Map<String, String> ipToHostname = new HashMap<String, String>();

		for (String ip : ips) {
			String hostname = m_hostinfoService.queryHostnameByIp(ip);

			if (hostname != null && !hostname.equalsIgnoreCase("null")) {
				ipToHostname.put(ip, hostname);
			}
		}

		return ipToHostname;
	}

	public String getIpToHostnameStr() {
		return new JsonBuilder().toJson(getIpToHostname());
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}
}
