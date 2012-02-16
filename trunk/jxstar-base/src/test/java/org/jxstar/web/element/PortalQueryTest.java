/*
 * PortalQueryTest.java 2010-12-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;


import org.jxstar.service.query.PortalQuery;
import org.jxstar.test.AbstractTest;

/**
 * 首页配置信息测试类
 *
 * @author TonyTan
 * @version 1.0, 2010-12-30
 */
public class PortalQueryTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PortalQuery portal = new PortalQuery();
		portal.getPortalJson("administrator");
	}

}
