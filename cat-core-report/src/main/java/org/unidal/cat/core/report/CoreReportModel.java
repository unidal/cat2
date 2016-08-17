package org.unidal.cat.core.report;

import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.spi.Report;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;

public abstract class CoreReportModel<P extends Page, A extends Action, M extends CoreReportContext<?>> extends
      ViewModel<P, A, M> {
   private transient String m_id;

   private transient String m_group;

   private GroupBar m_groupBar;

   public CoreReportModel(String id, M ctx) {
      super(ctx);

      m_id = id;
      m_group = ctx.getPayload().getGroup();
   }

   public abstract String getDomain();

   /* used by report-navbar.tag */
   public GroupBar getGroupBar() {
      if (m_groupBar == null) {
         PlexusContainer container = ContainerLoader.getDefaultContainer();

         try {
            GroupBar groupBar = container.lookup(GroupBar.class, "domain");
            Report report = getReport();
            Set<String> items = getItems();

            groupBar.initialize(report.getDomain(), m_group, items);
            m_groupBar = groupBar;
         } catch (Exception e) {
            Cat.logError(e);
         }
      }

      return m_groupBar;
   }

   public String getId() {
      return m_id;
   }

   protected abstract Set<String> getItems();

   /* used by report-navbar.tag */
   public abstract Report getReport();
}
