package org.unidal.cat.core.alert.metric.handler;

enum Functor {
   NOW {
      @Override
      public double evaluate(TimeWindow window) {
         return window.getValue(0);
      }
   },

   SUM {
      @Override
      public double evaluate(TimeWindow window) {
         int len = window.getLength();
         double sum = 0;

         for (int i = 0; i < len; i++) {
            sum += window.getValue(i);
         }

         return sum;
      }
   },

   AVG {
      @Override
      public double evaluate(TimeWindow window) {
         int len = window.getLength();
         double sum = 0;

         for (int i = 0; i < len; i++) {
            sum += window.getValue(i);
         }

         return sum / len;
      }
   },

   MIN {
      @Override
      public double evaluate(TimeWindow window) {
         int len = window.getLength();
         double min = window.getValue(0);

         for (int i = 1; i < len; i++) {
            double value = window.getValue(i);

            if (value < min) {
               min = value;
            }
         }

         return min;
      }
   },

   MAX {
      @Override
      public double evaluate(TimeWindow window) {
         int len = window.getLength();
         double max = window.getValue(0);

         for (int i = 1; i < len; i++) {
            double value = window.getValue(i);

            if (value > max) {
               max = value;
            }
         }

         return max;
      }
   };

   public static Functor getByName(String name) {
      for (Functor value : values()) {
         if (value.name().equalsIgnoreCase(name)) {
            return value;
         }
      }

      return NOW;
   }

   public abstract double evaluate(TimeWindow window);
}