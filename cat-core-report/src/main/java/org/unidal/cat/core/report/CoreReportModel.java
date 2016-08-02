package org.unidal.cat.core.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.core.report.menu.Menu;
import org.unidal.cat.core.report.menu.MenuManager;
import org.unidal.cat.core.report.nav.NavigationBar;
import org.unidal.cat.spi.ReportPeriod;
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
	private transient ReportPeriod m_period;

	private transient Throwable m_exception;

	private transient ProjectService m_projectService;

	private transient HostinfoService m_hostinfoService;

	private transient List<Menu> m_menus;

	public CoreReportModel(M ctx) {
		super(ctx);

		try {
			MenuManager manager = ContainerLoader.getDefaultContainer().lookup(MenuManager.class);

			m_menus = manager.getMenus(ctx);
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_projectService = ContainerLoader.getDefaultContainer().lookup(ProjectService.class);
			m_hostinfoService = ContainerLoader.getDefaultContainer().lookup(HostinfoService.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public List<Menu> getMenus() {
		return m_menus;
	}

	public List<NavigationBar> getBars() {
		if (m_period.isHour()) {
			return NavigationBar.getHourlyBars();
		} else {
			return NavigationBar.getHistoryBars();
		}
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	public NavigationBar getCurrentBar() {
		return NavigationBar.getByPeriod(m_period);
	}

	public abstract String getDomain();

	public Map<String, Department> getDomainGroups() {
		return m_projectService.findDepartments(getDomains());
	}

	public abstract Collection<String> getDomains();

	// required by report tag
	public Throwable getException() {
		return m_exception;
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

	public ReportPeriod getPeriod() {
		return m_period;
	};

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	public void setPeriod(ReportPeriod period) {
		m_period = period;
	}
}
