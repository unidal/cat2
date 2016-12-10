package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.route.entity.RoutePolicy;
import org.unidal.cat.config.route.entity.ServerNode;
import org.unidal.helper.Inets;

import com.dianping.cat.message.spi.MessageTree;

public class DefaultClientConfiguration implements ClientConfiguration {
   private String m_domain;

   private RoutePolicy m_policy;

   private double m_sampleRatio = 1.0;

   public DefaultClientConfiguration() {
      m_policy = new RoutePolicy();
   }

   public DefaultClientConfiguration(RoutePolicy policy) {
      m_policy = policy;
   }

   public void addServerForTree(String ip, Integer port) {
      ServerNode node = new ServerNode();

      node.setType("tree").setIp(ip).setPort(port).setEnabled(true);
      m_policy.addServerNode(node);
   }

   @Override
   public String getDomain() {
      return m_domain;
   }

   @Override
   public int getMaxMessageLines() {
      return m_policy.getMaxMessageLines();
   }

   @Override
   public long getRefreshInterval() {
      return TimeUnit.MINUTES.toMillis(1); // 1 minute
   }

   @Override
   public double getSampleRatio() {
      return m_sampleRatio;
   }

   @Override
   public String getServerConfigUrl() {
      for (ServerNode node : m_policy.getServerNodes()) {
         if (node.isEnabled() && node.getType().equals("http")) {
            return String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=json", node.getIp().trim(),
                  node.getPort(), m_domain, Inets.IP4.getLocalHostAddress());
         }
      }

      return null;
   }

   @Override
   public List<String> getServersForPlugin() {
      List<String> servers = new ArrayList<String>();

      for (ServerNode node : m_policy.getServerNodes()) {
         if (node.isEnabled() && node.getType().equals("http")) {
            servers.add(node.getIp() + ":" + node.getPort());
         }
      }

      return servers;
   }

   @Override
   public List<InetSocketAddress> getServersForTree() {
      List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

      for (ServerNode node : m_policy.getServerNodes()) {
         if (node.isEnabled() && node.getType().equals("tree")) {
            servers.add(new InetSocketAddress(node.getIp(), node.getPort()));
         }
      }

      return servers;
   }

   @Override
   public int getTaggedTransactionCacheSize() {
      return 1024; // TODO
   }

   @Override
   public boolean isBlocked() {
      return m_policy.isBlocked();
   }

   @Override
   public boolean isDumpLockedThread() {
      return m_policy.getDumpLockedThread();
   }

   @Override
   public boolean isEnabled() {
      return m_policy.isEnabled();
   }

   public void setDomain(String domain) {
      m_domain = domain;
   }

   public void setEnabled(boolean enabled) {
      m_policy.setEnabled(enabled);
   }

   @Override
   public boolean isAtomic(MessageTree tree) {
      return tree.canDiscard() && false; //TODO && m_atomicTreeParser.isAtomicMessage(tree)
   }
}
