package org.unidal.cat.core.message.provider;

import java.util.Map;

import com.dianping.cat.message.Transaction;

public interface MessageContext {
   public String buildURL(String serverUriPrefix);

   public void destroy();

   public int getIntProperty(String string, int i);

   public Transaction getParentTransaction();

   public Map<String, String> getProperties();

   public String getProperty(String property, String defaultValue);

   public void setParentTransaction(Transaction parent);

   public MessageContext setProperty(String property, String newValue);

   public boolean isLocal();
}
