package org.unidal.cat.config.internals;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainProvider.class)
public class DefaultDomainProvider implements DomainProvider {
   @Inject
   private Settings m_settings;

   @Override
   public String getDomain() {
      return m_settings.getDomain();
   }
}
