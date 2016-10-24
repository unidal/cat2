package org.unidal.cat.core.alert.metric.handler;

enum Operator {
   EQ("=") {
      @Override
      public boolean evaluate(double v1, double v2) {
         return Math.abs(v1 - v2) < 1e-6;
      }
   },

   GE("<=") {
      @Override
      public boolean evaluate(double v1, double v2) {
         return v1 <= v2;
      }
   },

   LE(">=") {
      @Override
      public boolean evaluate(double v1, double v2) {
         return v1 >= v2;
      }
   };

   private String m_display;

   private Operator(String display) {
      m_display = display;
   }

   public static Operator getByName(String name) {
      for (Operator value : values()) {
         if (value.name().equalsIgnoreCase(name)) {
            return value;
         }
      }

      return EQ;
   }

   public abstract boolean evaluate(double v1, double v2);

   public String getDisplay() {
      return m_display;
   }
}