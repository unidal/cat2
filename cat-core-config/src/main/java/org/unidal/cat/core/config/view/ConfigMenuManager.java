package org.unidal.cat.core.config.view;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.view.menu.AbstractMenuManager;
import org.unidal.cat.core.view.menu.MenuLinkBuilder;
import org.unidal.cat.core.view.menu.MenuManager;
import org.unidal.lookup.annotation.Named;
import org.unidal.web.mvc.ActionContext;

@Named(type = MenuManager.class, value = "config")
public class ConfigMenuManager extends AbstractMenuManager implements Initializable {
   @Override
   public void initialize() throws InitializationException {
      super.menu("config", "Configuration", "fa fa-cogs", //
            new MenuLinkBuilder() {
               @Override
               public String build(ActionContext<?> ctx) {
                  return ctx.getQuery().uri("/config").toString();
               }
            });
   }
}
