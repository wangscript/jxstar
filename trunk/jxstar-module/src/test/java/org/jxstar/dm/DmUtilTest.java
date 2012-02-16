/*
 * DmUtilTest.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;


import org.jxstar.dm.util.DmUtil;
import org.jxstar.test.AbstractTest;

/**
 * 数据库配置工具类测试。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class DmUtilTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DmUtil.existTable("test_table", "default");
		String sql = " --absdfdsfsdf\rsdfsdfc\r\n select * from abc where d = '-ddd';\r\n --bbsdfsdfsdfsdfdsfb\r\n update -\raa set a = b;\r\n";
		String sql1 = DmUtil.parseSql(sql);
		
		System.out.println("---sql1=" + sql1);
		
		boolean b = DmUtil.existTable("JZ_ACCOUNT", "default");
		System.out.println("---bb=" + b);
	}

}
