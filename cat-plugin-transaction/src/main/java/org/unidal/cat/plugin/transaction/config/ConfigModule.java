package org.unidal.cat.plugin.transaction.config;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "config", defaultInboundAction = "transaction", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.plugin.transaction.config.page.Handler.class
})
public class ConfigModule extends AbstractModule {

}
