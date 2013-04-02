/*
 * Log.java 2008-3-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.log;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jxstar.util.FileUtil;

/**
 * 原是采用java.util.log的日志类，由于文件追加设置失效，每次重新输出日志时总是把原文件内容清除了，
 * 所以换成了log4j包。
 * 
 * 封装后的日志对象，支持调式信息、进度信息、异常信息。
 * 在开发期间输出各级信息，如果发布到生产系统中后只要显示进度信息与异常信息就行了，
 * 修改配置文件内容（/conf/log.properties）如下：
 * .level= INFO
 * 
 * 输出的消息中可以带参数，参数的标示为：{0},{1},{2}... 分别表示参数1、参数2、参数3...
 * 
 * 在输出异常信息时，不要使用e.printStackTrace()方式输出异常，而要使用：
 * _log.showError(e) 的方式输出异常，这样以后在系统的日志文件中能取得这些异常信息，
 * 方便查找系统问题。
 * 
 * showInfo用于输出系统装载进度信息，在业务组件中不要使用。
 * 
 * @author TonyTan
 * @version 1.0, 2008-3-23
 */
public class Log {
	private static Log _log = new Log();
	//日志输出对象
	private Logger _logger = Logger.getRootLogger();
	
	private Log() {}
	
	public void init(String logfile) {
	    init(null, logfile);
	}
	/**
	 * 由于logging在tomcat下使用了LogManager，
	 * 造成本系统的配置文件失效，所以在本系统重新装载配置信息。
	 * @param realPath -- 实际路径，一般是类加载路径
	 * @param logfile
	 */
	public void init(String realPath, String logfile) {
	    if (realPath == null || realPath.length() == 0) {
	        realPath = FileUtil.getClassPath();
	    }
		if (logfile == null || logfile.length() == 0) {
			logfile = "log.properties";
		}
		
		logfile = realPath + logfile;
		PropertyConfigurator.configure(logfile);
	}
	
	/**
	 * 或取Logger日志对象。
	 * @return
	 */
	public static Log getInstance() {
		return _log;
	}
	
	/**
	 * 输出调试信息。
	 * @param sInfo
	 */
	public void showDebug(String sInfo) {
		_logger.debug(sInfo);
	}
	
	/**
	 * 输出调试信息，带一个参数数组。
	 * @param sInfo
	 * @param params
	 */
	public void showDebug(String sInfo, Object... params) {
		sInfo = MessageFormat.format(sInfo, params);
		_logger.debug(sInfo);
	}
	
	/**
	 * 输出系统装载进度信息。
	 * @param sInfo
	 */
	public void showInfo(String sInfo) {
		_logger.info(sInfo);
	}	
	
	/**
	 * 输出警告信息。
	 * @param sInfo
	 */
	public void showWarn(String sInfo) {
		sInfo = traceMethod() + "\nwarn: " + sInfo;
		_logger.warn(sInfo);
	}
	
	/**
	 * 输出警告信息，带一个参数数组。
	 * @param sInfo
	 * @param asParam
	 */
	public void showWarn(String sInfo, Object... params) {
		sInfo = MessageFormat.format(sInfo, params);
		showWarn(sInfo);
	}
	
	/**
	 * 输出异常信息。
	 * @param sInfo
	 */
	public void showError(String sInfo) {
		_logger.error(traceMethod() + "\nerror: " + sInfo);
	}
	
	/**
	 * 输出异常信息，带一个参数数组。
	 * @param sInfo
	 * @param asParam
	 */
	public void showError(String sInfo, Object... params) {
		sInfo = MessageFormat.format(sInfo, params);
		showError(sInfo);
	}
	
	/**
	 * 输出异常信息，带一个异常对象。
	 * @param sInfo		-- 异常信息 
	 * @param e			-- 异常对象
	 */
	public void showError(String sInfo, Throwable e) {
		sInfo = traceMethod() + "\nerror: " + sInfo;
		_logger.error(sInfo, e);
	}
	
	/**
	 * 输出异常信息，带一个异常对象。 
	 * @param e -- 异常对象
	 */
	public void showError(Throwable e) {
		_logger.error(traceMethod(), e);
	}
	
	/**
	 * 找到调用日志方法的类与方法，在调式信息栈中取信息。
	 */
	private String traceMethod() {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		//先找到jxstar的日志对象
		int ix = 0;
		while (ix < stack.length) {
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if (cname.equals(Log.class.getName())) {
				break;
			}
			ix++;
		}
		//日志对象的下一个对象就是调用日志对象的类
		while (ix < stack.length) {
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if (!cname.equals(Log.class.getName())) {
				StringBuilder sb = new StringBuilder(cname);
				sb.append(".").append(frame.getMethodName()).append(" on line: ");
				sb.append(frame.getLineNumber());
				return sb.toString();
			}
			ix++;
		}
		
		return "";
	}	
}
