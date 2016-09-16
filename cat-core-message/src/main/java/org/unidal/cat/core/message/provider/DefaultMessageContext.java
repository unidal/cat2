package org.unidal.cat.core.message.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;

public class DefaultMessageContext implements MessageContext {
   private MessageId m_id;

   private boolean m_useHdfs;

   private Map<String, String> m_properties;

   private ThreadLocal<Transaction> m_parent = new ThreadLocal<Transaction>();

   public DefaultMessageContext(MessageId id, boolean useHdfs) {
      m_id = id;
      m_useHdfs = useHdfs;
   }

   @Override
   public String buildURL(String serverUriPrefix) {
      StringBuilder sb = new StringBuilder(256);

      sb.append(serverUriPrefix);

      if (!serverUriPrefix.endsWith("/")) {
         sb.append('/');
      }

      sb.append(m_id);

      if (m_properties != null && m_properties.size() > 0) {
         boolean first = true;

         sb.append('?');

         try {
            for (Map.Entry<String, String> e : m_properties.entrySet()) {
               String key = URLEncoder.encode(e.getKey(), "UTF-8");
               String value = URLEncoder.encode(e.getValue(), "UTF-8");

               if (first) {
                  first = false;
               } else {
                  sb.append('&');
               }

               sb.append(key).append('=').append(value);
            }
         } catch (UnsupportedEncodingException e) {
            Cat.logError(e);
         }
      }

      return sb.toString();
   }

   @Override
   public void destroy() {
      m_parent.remove();
      m_properties = null;
   }

   @Override
   public int getIntProperty(String property, int defaultValue) {
      String value = getProperty(property, null);

      if (value != null) {
         try {
            return Integer.parseInt(value);
         } catch (Exception e) {
            // ignore it
         }
      }

      return defaultValue;
   }

   @Override
   public Transaction getParentTransaction() {
      return m_parent.get();
   }

   @Override
   public Map<String, String> getProperties() {
      if (m_properties == null) {
         return Collections.emptyMap();
      } else {
         return m_properties;
      }
   }

   @Override
   public String getProperty(String property, String defaultValue) {
      if (m_properties == null) {
         return defaultValue;
      } else {
         String value = m_properties.get(property);

         if (value == null) {
            return defaultValue;
         } else {
            return value;
         }
      }
   }

   @Override
   public boolean isLocal() {
      if (m_useHdfs) {
         int hour = m_id.getHour();
         long current = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

         return current - hour < 2;
      } else {
         return true;
      }
   }

   @Override
   public void setParentTransaction(Transaction parent) {
      m_parent.set(parent);
   }

   @Override
   public MessageContext setProperty(String property, String newValue) {
      if (newValue == null) {
         if (m_properties != null) {
            m_properties.remove(property);
         }
      } else {
         if (m_properties == null) {
            m_properties = new LinkedHashMap<String, String>();
         }

         m_properties.put(property, newValue);
      }

      return this;
   }

   @Override
   public String toString() {
      return buildURL("");
   }
}
