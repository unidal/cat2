package com.dianping.cat.message.spi;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MessageId;

public interface MessageTree extends Cloneable {
	public MessageTree copy();

	public ByteBuf getBuffer();

	public String getDomain();
	
	public MessageId getFormatMessageId();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public String getParentMessageId();

	public String getRootMessageId();

	public String getSessionToken();

	public String getThreadGroupName();

	public String getThreadId();

	public String getThreadName();

	public boolean isSample();

	public void setDomain(String domain);

	public void setFormatMessageId(MessageId messageId);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setParentMessageId(String parentMessageId);

	public void setRootMessageId(String rootMessageId);

	public void setSample(boolean sample);

	public void setSessionToken(String sessionToken);

	public void setThreadGroupName(String name);
	
	public void setThreadId(String threadId);
	
	public void setThreadName(String id);
}
