package org.unidal.cat.core.document;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "doc", defaultInboundAction = "home", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.core.document.page.Handler.class
})
public class DocumentModule extends AbstractModule {

}
