/*
 * ToolBarTest.java 2009-11-16
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;


import org.jxstar.service.query.ToolbarQuery;
import org.jxstar.test.AbstractTest;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2009-11-16
 */
public class ToolBarTest extends AbstractTest {
	public static void main(String[] args) {
		//String realPath = "D:\\Tomcat6\\webapps\\jxstar\\";
		// TODO Auto-generated method stub
		//ComboData comboData = new ComboData();
		ToolbarQuery tool = new ToolbarQuery();
		tool.createJson("jxstar2", "spare_base_catalog", "grid", "");
		System.out.println("------------" + tool.getReturnData());
	}
}
