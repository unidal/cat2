package com.dianping.cat.message.internal;

import org.unidal.cat.message.tree.DefaultMessageTreeManager;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageManager;

@Named(type = MessageManager.class)
public class DefaultMessageManager extends DefaultMessageTreeManager implements MessageManager {
}
