package org.unidal.cat.internals;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public enum NullMessageProducer implements MessageProducer {
   INSTANCE;

   @Override
   public String createMessageId() {
      return "";
   }

   @Override
   public MessageManager getManager() {
      return null;
   }

   @Override
   public void logError(String message, Throwable cause) {
   }

   @Override
   public void logError(Throwable cause) {
   }

   @Override
   public void logEvent(String type, String name) {
   }

   @Override
   public void logEvent(String type, String name, String status, String nameValuePairs) {
   }

   @Override
   public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
   }

   @Override
   public void logMetric(String name, String status, String nameValuePairs) {
   }

   @Override
   public void logTrace(String type, String name) {
   }

   @Override
   public void logTrace(String type, String name, String status, String nameValuePairs) {
   }

   @Override
   public Event newEvent(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ForkedTransaction newForkedTransaction(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Heartbeat newHeartbeat(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Metric newMetric(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Trace newTrace(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Transaction newTransaction(String type, String name) {
      // TODO Auto-generated method stub
      return null;
   }
}
