package com.dianping.cat.status;

import java.util.Map;

public interface StatusExtension {

	/**
	 * just the description of status extension
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * set the uniq id ,such as cat-message-queue
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * if value is double type , cat will build graph per minute in heartbeat report
	 * 
	 * if value is string type , cat will record the lastest key value in heartbeat report
	 * 
	 * make sure it will not block the thread
	 * 
	 * @return
	 */
	public Map<String, String> getProperties();
}
