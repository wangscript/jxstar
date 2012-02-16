/*
 * AbstractTest.java 2009-10-5
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.test;

import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 基础测试类，用于框架开发用。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-5
 */
public class AbstractTest {
	protected static String path = "D:/tomcat6/webapps/jxstar/WEB-INF/classes/";
	
	static {
	    System.out.println("..........classpath=" + path);
        String configFile = "conf/server.xml";
        
        //加载日志配置
        String logFile = "conf/log.properties";
        Log.getInstance().init(path, logFile);
        
        //加载系统配置
        SystemInitUtil.initSystem(path, configFile, false);   
        
		//SystemVar.REALPATH = path;
	}
}
