package com.dianping.cat.message.internal;

public class MessageId {
   private String m_domain;

   private String m_ipAddressInHex;

   private int m_hour;

   private int m_index;

   public MessageId(String domain, String ipAddressInHex, int hour, int index) {
      m_domain = domain;
      m_ipAddressInHex = ipAddressInHex;
      m_hour = hour;
      m_index = index;

      validate(domain);
   }

   public static MessageId parse(String messageId) {
      int index = -1;
      int hour = -1;
      String ipAddressInHex = null;
      String domain = null;
      int len = messageId == null ? 0 : messageId.length();
      int part = 4;
      int end = len;

      try {
         for (int i = end - 1; i >= 0; i--) {
            char ch = messageId.charAt(i);

            if (ch == '-') {
               switch (part) {
               case 4:
                  index = Integer.parseInt(messageId.substring(i + 1, end));
                  end = i;
                  part--;
                  break;
               case 3:
                  hour = Integer.parseInt(messageId.substring(i + 1, end));
                  end = i;
                  part--;
                  break;
               case 2:
                  ipAddressInHex = messageId.substring(i + 1, end);
                  domain = messageId.substring(0, i);
                  part--;
                  break;
               default:
                  break;
               }
            }
         }
      } catch (NumberFormatException e) {
         throw new RuntimeException("Invalid message ID format: " + messageId, e);
      }

      if (domain == null || ipAddressInHex == null || hour < 0 || index < 0) {
         throw new RuntimeException("Invalid message ID format: " + messageId);
      } else {
         return new MessageId(domain, ipAddressInHex, hour, index);
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof MessageId) {
         MessageId o = (MessageId) obj;

         if (!m_domain.equals(o.m_domain)) {
            return false;
         }

         if (!m_ipAddressInHex.equals(o.m_ipAddressInHex)) {
            return false;
         }

         if (m_hour != o.m_hour) {
            return false;
         }

         if (m_index != o.m_index) {
            return false;
         }

         return true;
      }

      return false;
   }

   public String getDomain() {
      return m_domain;
   }

   public int getHour() {
      return m_hour;
   }

   public int getIndex() {
      return m_index;
   }

   public String getIpAddress() {
      StringBuilder sb = new StringBuilder(16);
      String local = m_ipAddressInHex;
      int length = local.length();

      for (int i = 0; i < length; i += 2) {
         char ch1 = local.charAt(i);
         char ch2 = local.charAt(i + 1);
         int value = 0;

         if (ch1 >= '0' && ch1 <= '9') {
            value += (ch1 - '0') << 4;
         } else {
            value += ((ch1 - 'a') + 10) << 4;
         }

         if (ch2 >= '0' && ch2 <= '9') {
            value += ch2 - '0';
         } else {
            value += (ch2 - 'a') + 10;
         }

         if (sb.length() > 0) {
            sb.append('.');
         }

         sb.append(value);
      }

      return sb.toString();
   }

   public String getIpAddressInHex() {
      return m_ipAddressInHex;
   }

   public int getIpAddressValue() {
      String local = m_ipAddressInHex;
      int length = local.length();
      int ip = 0;

      for (int i = 0; i < length; i += 2) {
         char ch1 = local.charAt(i);
         char ch2 = local.charAt(i + 1);
         int value = 0;

         if (ch1 >= '0' && ch1 <= '9') {
            value += (ch1 - '0') << 4;
         } else {
            value += ((ch1 - 'a') + 10) << 4;
         }

         if (ch2 >= '0' && ch2 <= '9') {
            value += ch2 - '0';
         } else {
            value += (ch2 - 'a') + 10;
         }

         ip = (ip << 8) + value;
      }

      return ip;
   }

   public long getTimestamp() {
      return m_hour * 3600 * 1000L;
   }

   @Override
   public int hashCode() {
      int result = 1;

      result = 31 * result + ((m_domain == null) ? 0 : m_domain.hashCode());
      result = 31 * result + ((m_ipAddressInHex == null) ? 0 : m_ipAddressInHex.hashCode());
      result = 31 * result + m_hour;
      result = 31 * result + m_index;

      return result;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(m_domain.length() + 24);

      sb.append(m_domain);
      sb.append('-');
      sb.append(m_ipAddressInHex);
      sb.append('-');
      sb.append(m_hour);
      sb.append('-');
      sb.append(m_index);

      return sb.toString();
   }

   void validate(String domain) {
      int len = domain.length();

      for (int i = 0; i < len; i++) {
         char ch = domain.charAt(i);

         if (Character.isJavaIdentifierPart(ch) || ch == '.') {
            continue;
         } else {
            throw new RuntimeException("Invalid domain of message ID: " + this);
         }
      }
   }
}
