/*
 * SqlFilterTest.java 2011-3-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import org.jxstar.service.util.SqlFilter;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2011-3-1
 */
public class SqlFilterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String whereSql = " system. , abc, sys., drop , delete , update , create ";

		whereSql = SqlFilter.filter(whereSql);
		System.out.println("=========" + whereSql);
	}

}
