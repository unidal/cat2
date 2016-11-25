package com.dianping.cat.message.internal;

import org.unidal.cat.message.tree.DefaultMessageTreeProducer;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.MessageProducer;

@Named(type = MessageProducer.class)
public class DefaultMessageProducer extends DefaultMessageTreeProducer implements MessageProducer {
}
