/*
 * MenuQueryTest.java 2010-3-7
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;


import org.jxstar.service.query.MenuQuery;
import org.jxstar.test.AbstractTest;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2010-3-7
 */
public class MenuQueryTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MenuQuery menu = new MenuQuery();
		
		menu.createMainMenu("jxstar2");
	}

}
