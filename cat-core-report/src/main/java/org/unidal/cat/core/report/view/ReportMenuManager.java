package org.unidal.cat.core.report.view;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.view.menu.AbstractMenuManager;
import org.unidal.cat.core.view.menu.MenuManager;
import org.unidal.lookup.annotation.Named;

@Named(type = MenuManager.class, value = "report")
public class ReportMenuManager extends AbstractMenuManager implements Initializable {
   @Override
   public void initialize() throws InitializationException {
   }
}
