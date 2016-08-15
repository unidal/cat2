package org.unidal.cat.plugin.transactions.report;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "transactions", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

org.unidal.cat.plugin.transactions.report.page.Handler.class
})
public class ReportModule extends AbstractModule {

}
