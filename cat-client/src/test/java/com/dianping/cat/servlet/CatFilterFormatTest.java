package com.dianping.cat.servlet;

import org.junit.Test;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class CatFilterFormatTest {

	@Test
	public void test() {
		Exception e = new Exception();
		
		Cat.logError(e);
		Cat.logError(e);
		Cat.logError(e);
		Cat.logError(e);
		
		
		for (int i = 0; i < 10000; i++) {
			getRequestURI("http://cat.dianpingoa.com/cat/123.0/p");
		}
		System.out.println(getRequestURI("http://cat.dianpingoa.com/cat/r/p"));
		System.out.println(getRequestURI("http://cat.dianpingoa.com/123/r/123"));
		System.out.println(getRequestURI("http://cat.dianpingoa.com/234/126.0/p"));
		System.out.println(getRequestURI("http://cat.dianpingoa.com/456/2/p"));
		System.out.println(getRequestURI("/api/v1/mta/partner/3197852/query"));
		System.out.println(getRequestURI("/api/v1/mta/ba/partner/3010735/effective/financial"));
	}

	private String getRequestURI(String requestURI) {
		try {
			StringBuilder sb = new StringBuilder();
			String[] tabs = requestURI.split("/");
			boolean first = true;

			for (String tab : tabs) {
				if (first) {
					first = false;
				} else {
					sb.append('/');
				}
				if (StringUtils.isNotEmpty(tab) && isNumber(tab)) {
					sb.append("{num}");
				} else {
					sb.append(tab);
				}
			}

			return sb.toString();
		} catch (Exception e) {
			return requestURI;
		}
	}

	private boolean isNumber(String tab) {
		try {
			Double.parseDouble(tab);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
