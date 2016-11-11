package org.unidal.cat.core.alert.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertConfiguration.class)
public class DefaultAlertConfiguration implements Initializable, AlertConfiguration {
   private ConcurrentMap<String, Boolean> m_servers = new ConcurrentHashMap<String, Boolean>();

   private String m_serverUriPattern = "http://%s/cat/alert/service?op=binary";

   @Override
   public long getAlertCheckInterval() {
      return TimeUnit.MINUTES.toMillis(1); // 1m
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
   public String getServerUri(String server) {
      return String.format(m_serverUriPattern, server);
   }

   @Override
   public void initialize() throws InitializationException {
      m_servers.putIfAbsent("127.0.0.1:2281", true);
   }

   @Override
   public boolean isEnabled() {
      return true;
   }
}
