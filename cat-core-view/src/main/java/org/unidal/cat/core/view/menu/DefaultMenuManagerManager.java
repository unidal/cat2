package org.unidal.cat.core.view.menu;

import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = MenuManagerManager.class)
public class DefaultMenuManagerManager extends ContainerHolder implements MenuManagerManager {
   private Map<String, MenuManager> m_cached = new HashMap<String, MenuManager>();

   @Override
   public MenuManager config() {
      return getMenuManager("config");
   }

   @Override
   public MenuManager document() {
      return getMenuManager("document");
   }

   @Override
   public synchronized MenuManager getMenuManager(String id) {
      MenuManager manager = m_cached.get(id);

      if (manager == null) {
         manager = lookup(MenuManager.class);
         m_cached.put(id, manager);
      }

      return manager;
   }

   @Override
   public MenuManager report() {
      return getMenuManager("report");
   }
}
