package org.unidal.cat.core.alert.page;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "alert", defaultInboundAction = "home", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.core.alert.page.home.Handler.class,

org.unidal.cat.core.alert.page.service.Handler.class,

org.unidal.cat.core.alert.page.update.Handler.class
})
public class AlertModule extends AbstractModule {

}
