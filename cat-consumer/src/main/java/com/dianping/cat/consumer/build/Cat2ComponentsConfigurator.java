package com.dianping.cat.consumer.build;

import org.unidal.cat.plugin.event.EventPipeline;
import org.unidal.cat.plugin.routing.DomainHashStrategy;
import org.unidal.cat.plugin.routing.RoundRobinStrategy;
import org.unidal.cat.plugin.transaction.TransactionPipeline;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

public class Cat2ComponentsConfigurator extends AbstractResourceConfigurator {
    @Override
    public List<Component> defineComponents() {
        List<Component> all = new ArrayList<Component>();

        all.add(A(TransactionPipeline.class));
        all.add(A(EventPipeline.class));


        all.add(A(DomainHashStrategy.class));
        all.add(A(RoundRobinStrategy.class));

        return all;
    }
}
