package org.unidal.cat.core.config;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.cat.core.view.menu.Menu;
import org.unidal.cat.core.view.menu.MenuGroup;
import org.unidal.cat.core.view.menu.MenuManagerManager;
import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;

import com.dianping.cat.Cat;

public abstract class CoreConfigContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {
   private List<Menu> m_menus;

   private MenuGroup[] m_menuGroups;

   public CoreConfigContext() {
      try {
         m_menus = lookup(MenuManagerManager.class).config().getMenus(this);
      } catch (Exception e) {
         Cat.logError(e);
      }
   }

   /* used by config-menu.tag */
   public MenuGroup[] getMenuGroups() {
      return m_menuGroups;
   }

   /* used by config-menu.tag */
   public List<Menu> getMenus() {
      return m_menus;
   }

   public void initialize(HttpServletRequest request, HttpServletResponse response) {
      super.initialize(request, response);

      m_menuGroups = MenuGroup.values();
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
