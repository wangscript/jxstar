/*
 * SystemInitTest.java 2008-5-16
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;


import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 
 * 
 * @author TonyTan
 * @version 1.0, 2008-5-16
 */
public class SystemInitTest {

	public static void initSystem(String realPath) {
		String configFile = "conf/server.xml";
		
		//初始化日志对象
		String logFile = "conf/log.properties";
		Log.getInstance().init(realPath, logFile);
		
		//初始化系统对象
		SystemInitUtil.initSystem(realPath, configFile, false);	
	}
}
