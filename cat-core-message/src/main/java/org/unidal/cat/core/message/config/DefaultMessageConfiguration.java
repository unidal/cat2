package org.unidal.cat.core.message.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageConfiguration.class)
public class DefaultMessageConfiguration implements MessageConfiguration, Initializable {
   private ConcurrentMap<String, Boolean> m_servers = new ConcurrentHashMap<String, Boolean>();

   private String m_serverUriPrefixPattern = "http://%s/cat/message/";

   @Override
   public int getHdfsMaxStorageTime() {
      return 30;
   }

   @Override
   public int getRemoteCallConnectTimeoutInMillis() {
      return 1 * 1000; // 1s
   }

   @Override
   public int getRemoteCallReadTimeoutInMillis() {
      return 10 * 1000; // 10s
   }

   @Override
   public int getRemoteCallThreads() {
      return 3;
   }

   @Override
   public Map<String, Boolean> getServers() {
      return m_servers;
   }

   @Override
   public String getServerUriPrefix(String server) {
      return String.format(m_serverUriPrefixPattern, server);
   }

   @Override
   public void initialize() throws InitializationException {
      m_servers.putIfAbsent("127.0.0.1:2281", true);
   }

   @Override
   public boolean isUseHdfs() {
      return false;
   }
}
