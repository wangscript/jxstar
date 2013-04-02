/*
 * AbstractTest.java 2009-10-5
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.test;

import org.jxstar.util.config.SystemVar;
import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 基础测试类，用于框架开发用。
 * 在测试类中执行main中的测试方法前，必须先执行init(appPath);
 *
 * @author TonyTan
 * @version 1.0, 2009-10-5
 */
public class AbstractTest {
	/**
	 * 第一个参数为：程序路径，缺省为：d:/works/jxstar/jxstar-webapp/src/main/webapp
	 * 第二格参数为：配置文件路径，缺省为：d:/works/jxstar/jxstar-webapp/src/main/webapp/WEB-INF/classes/
	 * @param path
	 */
	public static void init(String... path) {
		String realPath = "d:/works/jxstar/jxstar-webapp/src/main/webapp";
		if (path.length > 0 && path[0] != null && path[0].length() > 0) {
			realPath = path[0];
		}
		String classPath = realPath + "/WEB-INF/classes/";
		if (path.length > 1 && path[1] != null && path[1].length() > 0) {
			classPath = path[1];
		}
		SystemVar.REALPATH = realPath;
		
	    System.out.println("..........classpath=" + classPath);
        String configFile = "conf/server.xml";
        
        //加载日志配置
        String logFile = "conf/log.properties";
        Log.getInstance().init(classPath, logFile);
        
        //加载系统配置
        SystemInitUtil.initSystem(classPath, configFile, false);   
	}
	
	public static void print(String test) {
		System.out.println(test);
	}
}
