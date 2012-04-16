/*
 * CodeCreatorTest.java 2010-3-27
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;


import org.jxstar.test.AbstractTest;
import org.jxstar.util.key.CodeCreator;

/**
 * 编号测试类
 *
 * @author TonyTan
 * @version 1.0, 2010-3-27
 */
public class CodeCreatorTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String code = CodeCreator.getInstance().createCode("car_app");
		System.out.println("=================" + code);
	}

}
