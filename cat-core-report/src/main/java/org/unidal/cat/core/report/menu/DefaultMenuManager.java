package org.unidal.cat.core.report.menu;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Named;
import org.unidal.web.mvc.ActionContext;

@Named(type = MenuManager.class)
public class DefaultMenuManager implements MenuManager {
	private List<MenuDef> m_defs = new ArrayList<MenuDef>();

	@Override
	public List<Menu> getMenus(ActionContext<?> ctx) {
		List<Menu> menus = new ArrayList<Menu>();

		for (MenuDef def : m_defs) {
			menus.add(new DefaultMenu(def, ctx));
		}

		return menus;
	}

	@Override
	public void register(String id, String title, String styleClasses, MenuLinkBuilder builder) {
		m_defs.add(new MenuDef(id, title, styleClasses, builder));
	}
}
