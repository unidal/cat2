package org.unidal.cat.spring;

import javax.servlet.DispatcherType;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dianping.cat.servlet.CatFilter;

@Configuration
public class CatFilterAutoConfiguration {
   @Bean
   public FilterRegistrationBean catFilter() {
      FilterRegistrationBean filter = new FilterRegistrationBean();

      filter.setFilter(new CatFilter());
      filter.setName("cat-filter");
      filter.addUrlPatterns("/*");
      filter.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
            DispatcherType.ERROR);

      return filter;
   }
}
