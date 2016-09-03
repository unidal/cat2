package org.unidal.cat.core.view.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuDef {
   private String m_id;

   private String m_title;

   private String m_styleClasses;

   private boolean m_standalone;

   private List<MenuDef> m_subMenus = new ArrayList<MenuDef>();

   private MenuLinkBuilder m_builder;

   public MenuDef(String id, String title, String styleClasses, MenuLinkBuilder builder) {
      m_id = id;
      m_title = title;
      m_styleClasses = styleClasses;
      m_builder = builder;
      m_standalone = true;
   }

   public MenuLinkBuilder getBuilder() {
      return m_builder;
   }

   public String getId() {
      return m_id;
   }

   public String getStyleClasses() {
      return m_styleClasses;
   }

   public List<MenuDef> getSubMenus() {
      return m_subMenus;
   }

   public String getTitle() {
      return m_title;
   }

   public boolean isStandalone() {
      return m_standalone;
   }

   public void submenu(String id, String title, String styleClasses, MenuLinkBuilder builder) {
      m_subMenus.add(new MenuDef(id, title, styleClasses, builder));
   }
}
