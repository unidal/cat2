package org.unidal.cat.plugin.events;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.document.spi.Document;
import org.unidal.cat.core.view.menu.MenuLinkBuilder;
import org.unidal.cat.core.view.menu.MenuManagerManager;
import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;
import org.unidal.web.mvc.ActionContext;

@Named(type = Pipeline.class, value = EventsConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class EventsPipeline extends AbstractPipeline implements Initializable {
   @Override
   protected boolean hasAnalyzer() {
      return false;
   }

   @Override
   public void initialize() throws InitializationException {
      MenuManagerManager manager = lookup(MenuManagerManager.class);

      manager.report().menu(EventsConstants.NAME, "Events", "fa fa-flag", new MenuLinkBuilder() {
         @Override
         public String build(ActionContext<?> ctx) {
            return ctx.getQuery().uri("/r/es") //
                  .get("type").get("").get("name").get("").get("group").get("").toString();
         }
      });
      manager.config().submenu("config", EventsConstants.NAME, "Events", "fa fa-flag", //
            new MenuLinkBuilder() {
               @Override
               public String build(ActionContext<?> ctx) {
                  return ctx.getQuery().uri("/config/events").empty().toString();
               }
            });

      Document.USER.register(EventsConstants.NAME, "Event Summary");
   }
}
