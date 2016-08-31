package org.unidal.cat.core.view.menu;

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.ActionContext;

public class DefaultMenu implements Menu {
	private MenuDef m_def;

	private ActionContext<?> m_ctx;

	public DefaultMenu(MenuDef def, ActionContext<?> ctx) {
		m_def = def;
		m_ctx = ctx;
	}

	public String getId() {
		return m_def.getId();
	}

	@Override
	public String getLink() {
		return m_def.getBuilder().build(m_ctx);
	}

	public String getStyleClasses() {
		return m_def.getStyleClasses();
	}

	@Override
	public List<Menu> getSubMenus() {
		List<Menu> menus = new ArrayList<Menu>();

		for (MenuDef def : m_def.getSubMenus()) {
			menus.add(new DefaultMenu(def, m_ctx));
		}

		return menus;
	}

	public String getTitle() {
		return m_def.getTitle();
	}

	public boolean isStandalone() {
		return m_def.isStandalone();
	}
}
