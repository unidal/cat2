package com.dianping.cat.configuration;

import java.util.List;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.spi.MessageTree;

public interface ClientConfigManager {

	public String getDomain();

	public int getMaxMessageLength();

	public String getRouters();

	public double getSampleRatio();

	public List<Server> getServers();

	public int getTaggedTransactionCacheSize();

	public void initialize(ClientConfig config) ;

	public boolean isAtomicMessage(MessageTree tree) ;

	public boolean isBlock();
	
	public boolean isCatEnabled();
	
	public boolean isDumpLocked();
	
	public void refreshConfig();

}