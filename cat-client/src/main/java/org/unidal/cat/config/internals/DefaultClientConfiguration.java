package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.route.entity.RoutePolicy;
import org.unidal.cat.config.route.entity.ServerNode;

public class DefaultClientConfiguration implements ClientConfiguration {
   private RoutePolicy m_policy;

   public DefaultClientConfiguration() {
      m_policy = new RoutePolicy();
   }

   public DefaultClientConfiguration(RoutePolicy policy) {
      m_policy = policy;
   }

   public void addServerNode(String type, String ip, Integer port) {
      ServerNode node = new ServerNode();

      node.setType(type).setIp(ip).setPort(port).setEnabled(true);
      m_policy.addServerNode(node);
   }

   @Override
   public List<InetSocketAddress> getServerNodes(String type) {
      List<InetSocketAddress> nodes = new ArrayList<InetSocketAddress>();

      for (ServerNode node : m_policy.getServerNodes()) {
         if (node.isEnabled() && node.getType().equals(type)) {
            nodes.add(new InetSocketAddress(node.getIp(), node.getPort()));
         }
      }

      return nodes;
   }

   @Override
   public boolean isEnabled() {
      return m_policy.isEnabled();
   }

   public void setEnabled(boolean enabled) {
      m_policy.setEnabled(enabled);
   }
}
