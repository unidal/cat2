package org.unidal.cat.core.report.nav;

import java.util.List;

public interface GroupBar {
   public String getActiveGroup();

   public String getActiveGroupItem();

   public List<String> getActiveGroupItems();

   public List<String> getGroups();

   public String getId();

   public String getItemName();
}
