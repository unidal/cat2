package org.unidal.cat.core.report;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.cat.core.config.DomainOrgConfigService;
import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;
import org.unidal.cat.core.report.menu.Menu;
import org.unidal.cat.core.report.menu.MenuGroup;
import org.unidal.cat.core.report.menu.MenuManager;
import org.unidal.cat.core.report.nav.DomainBar;
import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.core.report.nav.TimeBar;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ActionContext;

import com.dianping.cat.Cat;

public abstract class CoreReportContext<T extends CoreReportPayload<?, ?>> extends ActionContext<T> {
   private ReportPeriod m_period;

   private List<Menu> m_menus;

   private MenuGroup[] m_menuGroups;

   private DomainOrgConfigModel m_domainOrgConfig;

   private DomainBar m_domainBar;

   public CoreReportContext() {
      try {
         m_menus = lookup(MenuManager.class).getMenus(this);
         m_domainOrgConfig = lookup(DomainOrgConfigService.class).getConfig();
      } catch (Exception e) {
         Cat.logError(e);
      }
   }

   /* used by report-navbar.tag */
   public TimeBar getActiveTimeBar() {
      return TimeBar.getByPeriod(m_period);
   }

   /* used by report-header.tag */
   public DomainBar getDomainBar() {
      return m_domainBar;
   }

   /* used by report-header.tag */
   public DomainOrgConfigModel getDomainOrgConfig() {
      return m_domainOrgConfig;
   }

   /* used by report-navbar.tag */
   public abstract GroupBar getGroupBar();

   /* used by report-menu.tag */
   public MenuGroup[] getMenuGroups() {
      return m_menuGroups;
   }

   /* used by report-menu.tag */
   public List<Menu> getMenus() {
      return m_menus;
   }

   /* used by report-navbar.tag */
   public ReportPeriod getPeriod() {
      return m_period;
   }

   /* used by report-navbar.tag */
   public List<TimeBar> getTimeBars() {
      if (m_period.isHour()) {
         return TimeBar.getHourlyBars();
      } else {
         return TimeBar.getHistoryBars();
      }
   }

   public void initialize(HttpServletRequest request, HttpServletResponse response) {
      super.initialize(request, response);

      String period = getHttpServletRequest().getParameter("period");

      m_period = ReportPeriod.getByName(period, ReportPeriod.HOUR);
      m_menuGroups = MenuGroup.values();
      m_domainBar = new DomainBar(request, response);
   }

   protected <S> S lookup(Class<S> role) {
      return (S) lookup(role, null);
   }

   protected <S> S lookup(Class<S> role, String hint) {
      PlexusContainer container = ContainerLoader.getDefaultContainer();

      try {
         return (S) container.lookup(role, hint);
      } catch (ComponentLookupException e) {
         throw new RuntimeException(String.format("Unable to lookup component(%s:%s)!", role.getName(), hint), e);
      }
   }
}
