/*
 * Log4jTest.java 2010-11-20
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.jxstar.util.log.Log;

/**
 * 由于java的Log包在输出日志文件时，设置文件内容追加的方式无效，所以不使用了。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-20
 */
public class Log4jTest {

	public static void main(String[] args) {
		Logger conLogger = Logger.getLogger("myConsole");
		conLogger.setAdditivity(false);
		
		Logger fileLogger = Logger.getLogger("myFile");
		fileLogger.setAdditivity(false);
        
        //Load the proerties using the PropertyConfigurator
        PropertyConfigurator.configure("d:/log4j.properties");
		
        //Log Messages using the Parent Logger
        conLogger.debug("Thie is a log message from the " + conLogger.getName());
        conLogger.info("Thie is a log message from the " + conLogger.getName());
        
        fileLogger.debug("Thie is a log message from the " + fileLogger.getName());
        fileLogger.info("Thie is a log message from the " + fileLogger.getName());
        fileLogger.info("ssssssss", new Exception("sdfsdf"));
        
        conLogger.info(Log.class.getName());
	}
}
