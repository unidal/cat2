package org.unidal.cat.core.report.nav;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.helper.Joiners;
import org.unidal.helper.Splitters;

public class DomainBar {
   private static final String CAT_DOMAINS = "CAT_DOMAINS";

   private List<String> m_recentDomains = new ArrayList<String>();

   private HttpServletResponse m_response;

   public DomainBar(HttpServletRequest request, HttpServletResponse response) {
      m_response = response;
      Cookie[] cookies = request.getCookies();

      if (cookies != null) {
         for (Cookie cookie : cookies) {
            if (cookie.getName().equals(CAT_DOMAINS)) {
               Splitters.by('|').trim().noEmptyItem().split(cookie.getValue(), m_recentDomains);
               break;
            }
         }
      }
   }

   public void addRecentDomain(String domain) {
      m_recentDomains.remove(domain);
      m_recentDomains.add(0, domain);

      for (int i = m_recentDomains.size() - 1; i >= 10; i--) {
         m_recentDomains.remove(i);
      }

      Cookie cookie = new Cookie(CAT_DOMAINS, Joiners.by('|').join(m_recentDomains));

      cookie.setPath("/");
      cookie.setMaxAge(365 * 86400); // one year to expire
      m_response.addCookie(cookie);
   }

   public List<String> getRecentDomains() {
      return m_recentDomains;
   }
}
