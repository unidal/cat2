package org.unidal.cat.core.config.system;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "system", defaultInboundAction = "config", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.core.config.system.page.Handler.class
})
public class SystemModule extends AbstractModule {

}
