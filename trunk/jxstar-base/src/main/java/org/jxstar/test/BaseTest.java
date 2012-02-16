/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.test;

import org.jxstar.util.log.Log;
import org.jxstar.util.system.SystemInitUtil;

/**
 * 项目中的测试环境初始化的工具类，在执行测试方法前，先执行初始化方法。
 *
 * @author TonyTan
 * @version 1.0, 2012-2-8
 */
public class BaseTest {
	/**
	 * 初始化系统环境，必须设置类路径，路径格式如：
	 * D:/tomcat6/webapps/jxstar/WEB-INF/classes/
	 */
	public static void init(String classesPath) {
		if (classesPath == null || classesPath.length() == 0) {
			System.out.println("classesPath属性为空，必须通过setClassesPath方法给classesPath赋值！");
			return;
		}
		
	    System.out.println("..........classes=" + classesPath);
        String configFile = "conf/server.xml";
        
        //加载日志配置
        String logFile = "conf/log.properties";
        Log.getInstance().init(classesPath, logFile);
        
        //加载系统配置
        SystemInitUtil.initSystem(classesPath, configFile, false);   
	}

}
