package org.unidal.cat.core.view.menu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ActionContext;

public abstract class AbstractMenuManager implements MenuManager {
   private Map<String, MenuDef> m_defs = new LinkedHashMap<String, MenuDef>();

   @Override
   public List<Menu> getMenus(ActionContext<?> ctx) {
      List<Menu> menus = new ArrayList<Menu>();

      for (MenuDef def : m_defs.values()) {
         menus.add(new DefaultMenu(def, ctx));
      }

      return menus;
   }

   @Override
   public MenuDef menu(String id, String title, String styleClasses, MenuLinkBuilder builder) {
      MenuDef def = m_defs.get(id);

      if (def == null) {
         def = new MenuDef(id, title, styleClasses, builder);
         m_defs.put(id, def);
      }

      return def;
   }

   @Override
   public void submenu(String menuId, String id, String title, String styleClasses, MenuLinkBuilder builder) {
      MenuDef def = m_defs.get(menuId);

      if (def == null) {
         throw new IllegalStateException(String.format("Menu(%s) is not defined!", menuId));
      }

      def.submenu(id, title, styleClasses, builder);
   }
}
