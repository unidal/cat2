package org.unidal.cat.core.config.service;

import org.unidal.lookup.annotation.Named;

@Named(type = ContactorService.class)
public class DefaultContactorService implements ContactorService {
   @Override
   public String getContactor(String type, String id) {
      // TODO
      if ("email".equals(type)) {
         if ("qmwu2000".equals(id)) {
            return "qmwu2000@gmail.com";
         }
      }

      throw new RuntimeException("Unknown contact type: " + type + "!");
   }
}
