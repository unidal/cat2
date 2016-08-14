package com.dianping.cat.message.spi.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class DefaultMessageTree implements MessageTree {
   private ByteBuf m_buf;

   private String m_domain;

   private String m_hostName;

   private String m_ipAddress;

   private Message m_message;

   private String m_messageId;

   private MessageId m_formatMessageId;

   private String m_parentMessageId;

   private String m_rootMessageId;

   private String m_sessionToken;

   private String m_threadGroupName;

   private String m_threadId;

   private String m_threadName;

   private boolean m_sample;

   private List<Transaction> m_transactions = new ArrayList<Transaction>();

   private List<Event> m_events = new ArrayList<Event>();

   private List<Heartbeat> m_heartbeats;

   private List<Metric> m_metrics;

   @Override
   public MessageTree copy() {
      MessageTree tree = new DefaultMessageTree();

      tree.setDomain(m_domain);
      tree.setHostName(m_hostName);
      tree.setIpAddress(m_ipAddress);
      tree.setMessageId(m_messageId);
      tree.setParentMessageId(m_parentMessageId);
      tree.setRootMessageId(m_rootMessageId);
      tree.setSessionToken(m_sessionToken);
      tree.setThreadGroupName(m_threadGroupName);
      tree.setThreadId(m_threadId);
      tree.setThreadName(m_threadName);
      tree.setMessage(m_message);
      tree.setSample(m_sample);

      return tree;
   }

   public ByteBuf getBuffer() {
      return m_buf;
   }

   @Override
   public String getDomain() {
      return m_domain;
   }

   public List<Event> getEvents() {
      return m_events;
   }

   @Override
   public MessageId getFormatMessageId() {
      return m_formatMessageId;
   }

   public List<Heartbeat> getHeartbeats() {
      if (m_heartbeats == null) {
         return Collections.emptyList();
      } else {
         return m_heartbeats;
      }
   }

   @Override
   public String getHostName() {
      return m_hostName;
   }

   @Override
   public String getIpAddress() {
      return m_ipAddress;
   }

   @Override
   public Message getMessage() {
      return m_message;
   }

   @Override
   public String getMessageId() {
      return m_messageId;
   }

   public List<Metric> getMetrics() {
      if (m_metrics == null) {
         return Collections.emptyList();
      } else {
         return m_metrics;
      }
   }

   @Override
   public String getParentMessageId() {
      return m_parentMessageId;
   }

   @Override
   public String getRootMessageId() {
      return m_rootMessageId;
   }

   @Override
   public String getSessionToken() {
      return m_sessionToken;
   }

   @Override
   public String getThreadGroupName() {
      return m_threadGroupName;
   }

   @Override
   public String getThreadId() {
      return m_threadId;
   }

   @Override
   public String getThreadName() {
      return m_threadName;
   }

   public List<Transaction> getTransactions() {
      return m_transactions;
   }

   @Override
   public boolean isSample() {
      return m_sample;
   }

   public void setBuffer(ByteBuf buf) {
      m_buf = buf;
   }

   @Override
   public void setDomain(String domain) {
      m_domain = domain;
   }

   @Override
   public void setFormatMessageId(MessageId messageId) {
      m_formatMessageId = messageId;
   }

   @Override
   public void setHostName(String hostName) {
      m_hostName = hostName;
   }

   @Override
   public void setIpAddress(String ipAddress) {
      m_ipAddress = ipAddress;
   }

   @Override
   public void setMessage(Message message) {
      m_message = message;
   }

   @Override
   public void setMessageId(String messageId) {
      if (messageId != null && messageId.length() > 0) {
         m_messageId = messageId;
      }
   }

   @Override
   public void setParentMessageId(String parentMessageId) {
      if (parentMessageId != null && parentMessageId.length() > 0) {
         m_parentMessageId = parentMessageId;
      }
   }

   @Override
   public void setRootMessageId(String rootMessageId) {
      if (rootMessageId != null && rootMessageId.length() > 0) {
         m_rootMessageId = rootMessageId;
      }
   }

   @Override
   public void setSample(boolean sample) {
      m_sample = sample;
   }

   @Override
   public void setSessionToken(String sessionToken) {
      if (sessionToken != null && sessionToken.length() > 0) {
         m_sessionToken = sessionToken;
      }
   }

   @Override
   public void setThreadGroupName(String threadGroupName) {
      m_threadGroupName = threadGroupName;
   }

   @Override
   public void setThreadId(String threadId) {
      m_threadId = threadId;
   }

   @Override
   public void setThreadName(String threadName) {
      m_threadName = threadName;
   }

   @Override
   public String toString() {
      PlainTextMessageCodec codec = new PlainTextMessageCodec();
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

      codec.encode(this, buf);
      codec.reset();
      return buf.toString(Charset.forName("utf-8"));
   }

   public void addMetric(Metric metric) {
      if (m_metrics == null) {
         m_metrics = new ArrayList<Metric>();
      }

      m_metrics.add(metric);
   }

   public void addHeartbeat(Heartbeat heartbeat) {
      if (m_heartbeats == null) {
         m_heartbeats = new ArrayList<Heartbeat>();
      }

      m_heartbeats.add(heartbeat);
   }
}
