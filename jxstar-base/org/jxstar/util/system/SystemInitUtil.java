/*
 * SystemInitUtil.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.system;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


import org.jxstar.dao.pool.DataSourceConfigManager;
import org.jxstar.service.define.FunctionDefineManger;
import org.jxstar.task.SystemLoader;
import org.jxstar.util.FileUtil;
import org.jxstar.util.config.ConfigParser;
import org.jxstar.util.config.SystemConfig;
import org.jxstar.util.config.SystemConfigParser;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsParam;

/**
 * 系统初始化的工具类.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings({"rawtypes"})
public class SystemInitUtil {
	//日志对象
	private static Log _log = Log.getInstance();
	
	public static boolean initSystem(String serverFile, boolean isReload) {
	    return initSystem(null, serverFile, isReload);
	}
	/**
	 * 初始化系统对象.
	 * @param realPath -- 实际路径，一般是类加载路径
	 * @param serverFile - 配置文件名
	 * @param isReload 是否重新装载
	 * @return boolean
	 */
	public static boolean initSystem(String realPath, 
	        String serverFile, boolean isReload) {
        if (realPath == null || realPath.length() == 0) {
            realPath = FileUtil.getClassPath();
        }
		long st = System.currentTimeMillis();
		_log.showInfo("jxstar system init start...");
		
		if (serverFile == null || serverFile.length() == 0) {
			_log.showInfo("init system param is null! ");
			return false;
		}
		
		//初始化系统核心配置文件：conf/server.xml
		ConfigParser parser = new SystemConfigParser();
		parser.init(realPath + serverFile);
		SystemConfig.init(parser.readConfig());
		
		//初始化数据源配置工具
		List<Map<String,String>> lsDataSource = SystemConfig.
					getConfigListByKey("DataSources");
		DataSourceConfigManager.getInstance().init(lsDataSource);
		
		//初始化系统变量配置
		SystemVar.init();
		
		//是否装载系统所有功能对象
		String isLoadFun = SystemVar.getValue("fun.define.loadall");
		if (isLoadFun.equals("1")) {
			FunctionDefineManger.getInstance().loadDefine();
		}
		
		//初始化读取配置文件的工具
		List<Map<String,String>> lsConfig = SystemConfig.
					getConfigListByKey("InitConfigs");
		loadConfigUtil(realPath, lsConfig);
		
		//初始化读取资源文件的工具
		List<Map<String,String>> lsResource = SystemConfig.
					getConfigListByKey("InitResources");
		loadResourceUtil(realPath, lsResource);	
		
		//装载系统任务, 重新装载时不装载系统任务
		if (isReload == false) {
			List<Map<String,String>> lsTasks = SystemConfig.
					getConfigListByKey("SystemTasks");
			loadSystemTaskConfig(realPath, lsTasks);
		}
		
		long ut = System.currentTimeMillis() - st;
		_log.showInfo("jxstar system init end in "+ ut +" ms...");
		return true;
	}
	
	/**
	 * 启动系统定义的任务.
	 * 
	 * @param realPath - 系统路径
	 * @param lsConfig - 任务配置信息
	 * @return
	 */
	private static boolean loadSystemTaskConfig(String realPath,
			List<Map<String,String>> lsConfig) {
		_log.showInfo("jxstar load system task object...");
		
		if (lsConfig == null || lsConfig.isEmpty()) {
			return true;
		}
		
		for (int i = 0; i < lsConfig.size(); i++) {
			Map<String,String> mpConfig = lsConfig.get(i);
			if (mpConfig == null || mpConfig.isEmpty()) continue;
			
			//是否加载
			String startup = mpConfig.get("startup");
			if (startup.equals("false")) continue;
			
			String className = mpConfig.get("classname");
			if (className == null || className.length() == 0) continue;
			
			//取参数值name1=value1;name2=value2
			Map<String,String> mpParam = FactoryUtil.newMap();
			String initParam = mpConfig.get("initparam");
			if (initParam != null && initParam.length() > 0) {
				String[] params = initParam.split(";");
				for (int j = 0; j < params.length; j++) {
					if (params[j].length() == 0) continue;
					
					String[] nv = params[j].split("=");
					if (nv.length < 2) continue;
					
					mpParam.put(nv[0], nv[1]);
				}
			}
			
			//系统路径作为参数
			mpParam.put(JsParam.REALPATH, realPath);
			
			//启动系统定义的任务
			loadSystemTask(className, mpParam);
		}
		
		return true;
	}
	
	/**
	 * 初始化读取配置信息的工具对象.
	 * 
	 * @param realPath - 配置文件绝对路径
	 * @param lsConfig - 配置信息
	 * @return boolean
	 */
	private static boolean loadConfigUtil(
			String realPath, List<Map<String,String>> lsConfig) {
		_log.showInfo("jxstar load config tool...");
		
		if (lsConfig == null || lsConfig.isEmpty()) {
			return true;
		}
		
		for (int i = 0; i < lsConfig.size(); i++) {
			Map<String,String> mpConfig = lsConfig.get(i);
			if (mpConfig == null || mpConfig.isEmpty()) continue;
			
			String sParserClass = mpConfig.get("parserclass");
			if (sParserClass == null || sParserClass.length() == 0) continue;
			
			String sFileName = mpConfig.get("configfile");
			if (sFileName == null || sFileName.length() == 0) continue;
			sFileName = realPath + sFileName;
			
			String sUtilClass = mpConfig.get("utilclass");
			if (sUtilClass == null || sUtilClass.length() == 0) continue;
			//初始化配置信息工具对象
			loadConfigUtil(sParserClass, sFileName, sUtilClass);
		}
		
		return true;
	}
	
	/**
	 * 初始化读取资源文件的工具.
	 * 
	 * @param realPath - 配置文件绝对路径
	 * @param lsResource - 资源文件
	 * @return boolean
	 */
	private static boolean loadResourceUtil(
			String realPath, List<Map<String,String>> lsResource) {
		_log.showInfo("jxstar load resource tool...");
		
		if (lsResource == null || lsResource.isEmpty()) {
			return true;
		}
		
		for (int i = 0; i < lsResource.size(); i++) {
			Map<String,String> mpResource = lsResource.get(i);
			if (mpResource == null || mpResource.isEmpty()) continue;
			
			String sResourceClass = mpResource.get("resourceclass");
			if (sResourceClass == null || sResourceClass.length() == 0) continue;
			
			String sFileName = mpResource.get("resourcefile");
			if (sFileName == null || sFileName.length() == 0) continue;
			sFileName = realPath + sFileName;
			
			//初始化配置信息工具对象
			loadResourceUtil(sResourceClass, sFileName);
		}
		
		return true;
	}
	
	/**
	 * 装载并启动系统任务.
	 * 
	 * @param className - 任务类名
	 * @param param - 任务参数
	 * @return boolean
	 */
	private static boolean loadSystemTask(String className, 
			Map<String,String> param) {
		if (className == null || className.length() == 0) {
			_log.showWarn("load system task classname is null! ");
			return false;
		}
		
		//系统任务的缺省路径
		String path = "org.jxstar.task.load.";
		try {
			//创建线程的class对象
			Class clzz = Class.forName(path + className);

			//创建任务实例		
			SystemLoader task = (SystemLoader) clzz.newInstance();
			//执行任务
			task.execute(param);
		} catch (Exception e) {
			_log.showError(e);
			return false;
		}
		
		return true;
	}

	/**
	 * 初始化配置信息工具对象.
	 * 
	 * @param sParserClass - 解析对象类名
	 * @param sFileName - 配置文件名
	 * @param sUtilClass - 配置信息工具对象类名
	 * @return boolean 
	 */
	private static boolean loadConfigUtil(String sParserClass, 
			String sFileName, String sUtilClass) {
		if (sParserClass == null || sParserClass.length() == 0 ||
				sFileName == null || sFileName.length() == 0 || 
				sUtilClass == null || sUtilClass.length() == 0) {
			_log.showWarn("load config util param is null! ");
			return false;
		}
		
		try {
			//创建解析工具的class对象
			Class<?> clzz = Class.forName(sParserClass);

			//创建解析工具的实例		
			ConfigParser clzzObj = (ConfigParser) clzz.newInstance();
			
			//配置文件解析对象解析文件出错
			clzzObj.init(sFileName);
			Map mpConfig = clzzObj.readConfig();
			if (mpConfig == null || mpConfig.isEmpty()) {
				_log.showWarn("read config file faild! ");
				return false;
			}
			
			//创建配置信息工具的class对象
			Class<?> clzzu  = Class.forName(sUtilClass);
			//调用配置信息工具的初始化方法
			Method method = clzzu.getMethod("init", Map.class);
			method.invoke(null, new Object[]{mpConfig});
		} catch (Exception e) {
			_log.showError(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 初始化资源工具对象.
	 * 
	 * @param sResourceClass - 解析对象类名
	 * @param sFileName - 资源文件名
	 * @return boolean 
	 */
	private static boolean loadResourceUtil(String sResourceClass, 
								String sFileName) {
		if (sResourceClass == null || sResourceClass.length() == 0 ||
				sFileName == null || sFileName.length() == 0) {
			_log.showWarn("load resource util param is null! ");
			return false;
		}
		
		try {
			//创建资源工具的class对象
			Class<?> clzz = Class.forName(sResourceClass);
			//调用资源工具的初始化方法
			Method method = clzz.getMethod("init", String.class);
			method.invoke(null, new Object[]{sFileName});
		} catch (Exception e) {
			_log.showError(e);
			return false;
		}
		
		return true;
	}
}
