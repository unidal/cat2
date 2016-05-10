package com.dianping.cat.consumer.build;

import org.unidal.cat.plugin.transaction.TransactionPipeline;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

public class Cat2ComponentsConfiguration extends AbstractResourceConfigurator {
    @Override
    public List<Component> defineComponents() {
        List<Component> all = new ArrayList<Component>();

        all.add(A(TransactionPipeline.class));

        return all;
    }
}
