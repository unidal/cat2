package org.unidal.cat.message.storage.internals;

public enum CompressType {
   GZIP("gzip"),

   DEFLATE("deflate"),

   SNAPPY("snappy");

   private String m_name;

   private CompressType(String name) {
      m_name = name;
   }

   public String getName() {
      return m_name;
   }

   public static CompressType getCompressTye(String name) {
      for (CompressType type : values()) {
         if (name.equals(type.getName())) {
            return type;
         }
      }
      return GZIP;
   }

}
