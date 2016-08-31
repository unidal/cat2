package org.unidal.cat.core.view.menu;

import java.util.List;

public interface Menu {
	public String getId();

	public String getLink();

	public String getStyleClasses();

	public List<Menu> getSubMenus();

	public String getTitle();

	public boolean isStandalone();
}
