/*
 * KeyCreatorTest.java 2011-2-22
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;


import org.jxstar.test.AbstractTest;
import org.jxstar.util.key.KeyCreator;

/**
 * 主键生成类测试
 *
 * @author TonyTan
 * @version 1.0, 2011-2-22
 */
public class KeyCreatorTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SystemInitTest.initSystem("d:/Tomcat6/webapps/jxstar");
		
		for (int i = 0; i < 1000; i++) {
			String key = KeyCreator.getInstance().createKey("fun_rule_param");
			
			System.out.println(key);
		}
	}
	
	public void testRandom() {
		for (int i = 0; i < 1000; i++) {
			String fix = createRandomNum();
			if (fix.length() < 3) {
				System.out.println("=========" + fix);
			}
			if (i % 1000 == 0)
				System.out.println("输出了" + i);
		}
	}
	
	private String createRandomNum() {
		int random =  (int) (Math.random() * 1000);
		StringBuilder fix = new StringBuilder(Integer.toString(random));
		
		int len = 3 - fix.length();
		for (int i = 0; i < len; fix.insert(0, '0'), i++);
		
		//fix.append('v');
		return fix.toString();
	}
}
