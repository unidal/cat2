package org.unidal.cat.core.alert.engine;

import java.util.Map;

public interface AlertContext {
   public <T> T getCell(int row, String property);

   public Map<String, Object> getRow(int row);

   public int getRows();
}
