package com.dianping.cat.build;

import org.unidal.cat.plugin.transaction.page.transform.AllReportDistributionBuilder;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

public class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
    @Override
    public List<Component> defineComponents() {
        List<Component> all = new ArrayList<Component>();

        all.add(A(AllReportDistributionBuilder.class));

        return all;
    }
}
