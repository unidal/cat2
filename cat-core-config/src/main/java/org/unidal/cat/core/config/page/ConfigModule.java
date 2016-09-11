package org.unidal.cat.core.config.page;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "config", defaultInboundAction = "update", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.core.config.page.service.Handler.class,

org.unidal.cat.core.config.page.update.Handler.class
})
public class ConfigModule extends AbstractModule {

}
