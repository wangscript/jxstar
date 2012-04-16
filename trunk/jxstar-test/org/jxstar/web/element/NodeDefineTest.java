/*
 * NodeDefineTest.java 2010-11-27
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.web.element;

import java.util.Map;

import org.jxstar.test.AbstractTest;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 
 *
 * @author TonyTan
 * @version 1.0, 2010-11-27
 */
public class NodeDefineTest extends AbstractTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//FunDefineBO define = new FunDefineBO();
		
		//String oldFunId = "sys_user_data";
		//String copyFunId = "sys_user_datax";
		Map<String,String> mpUser = FactoryUtil.newMap(); 
		mpUser.put("user_id", "jxstar2");
		//define.copyFun(oldFunId, copyFunId, mpUser);
	}

}
