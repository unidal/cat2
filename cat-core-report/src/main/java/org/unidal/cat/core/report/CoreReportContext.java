package org.unidal.cat.core.report;

import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.cat.core.report.menu.Menu;
import org.unidal.cat.core.report.menu.MenuGroup;
import org.unidal.cat.core.report.menu.MenuManager;
import org.unidal.cat.core.report.nav.TimeBar;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;

import com.dianping.cat.Cat;

public abstract class CoreReportContext<T extends ActionPayload<?, ?>> extends ActionContext<T> {
	private List<Menu> m_menus;

	private MenuGroup[] m_menuGroups;

	public CoreReportContext() {
		PlexusContainer container = ContainerLoader.getDefaultContainer();

		m_menuGroups = MenuGroup.values();

		try {
			m_menus = container.lookup(MenuManager.class).getMenus(this);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	/* used by report-navbar.tag */
	public MenuGroup[] getMenuGroups() {
		return m_menuGroups;
	}

	/* used by report-navbar.tag */
	public List<Menu> getMenus() {
		return m_menus;
	}

	/* used by report-content.tag */
	public ReportPeriod getPeriod() {
		String period = getHttpServletRequest().getParameter("period");

		return ReportPeriod.getByName(period, ReportPeriod.HOUR);
	}

	/* used by report-content.tag */
	public List<TimeBar> getTimeBars() {
		if (getPeriod().isHour()) {
			return TimeBar.getHourlyBars();
		} else {
			return TimeBar.getHistoryBars();
		}
	}
}
