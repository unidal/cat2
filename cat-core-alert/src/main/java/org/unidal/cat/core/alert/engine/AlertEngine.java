package org.unidal.cat.core.alert.engine;

import org.unidal.helper.Threads.Task;

public interface AlertEngine extends Task {
   public void register(AlertListener listener);
}
