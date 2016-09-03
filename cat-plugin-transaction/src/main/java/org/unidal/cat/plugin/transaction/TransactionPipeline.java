package org.unidal.cat.plugin.transaction;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.document.spi.Document;
import org.unidal.cat.core.view.menu.MenuLinkBuilder;
import org.unidal.cat.core.view.menu.MenuManagerManager;
import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;
import org.unidal.web.mvc.ActionContext;

@Named(type = Pipeline.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionPipeline extends AbstractPipeline implements Initializable {
   @Override
   public void initialize() throws InitializationException {
      MenuManagerManager manager = lookup(MenuManagerManager.class);

      manager.report().menu(TransactionConstants.NAME, "Transaction", "glyphicon glyphicon-time",
            new MenuLinkBuilder() {
               @Override
               public String build(ActionContext<?> ctx) {
                  return ctx.getQuery().uri("/r/t") //
                        .get("type").get("").get("name").get("").get("group").get("").toString();
               }
            });
      manager.config().menu("config", "Configuration", "fa fa-cogs", //
            new MenuLinkBuilder() {
               @Override
               public String build(ActionContext<?> ctx) {
                  return ctx.getQuery().uri("/system/config").toString();
               }
            }).submenu(TransactionConstants.NAME, "Transaction", "glyphicon glyphicon-time", //
            new MenuLinkBuilder() {
               @Override
               public String build(ActionContext<?> ctx) {
                  return ctx.getQuery().uri("/system/config/transaction").toString();
               }
            });

      Document.USER.register(TransactionConstants.NAME, "Transaction");
   }
}
