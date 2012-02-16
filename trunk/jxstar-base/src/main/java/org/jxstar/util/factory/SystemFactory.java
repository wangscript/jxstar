/*
 * SystemFactory.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.factory;

import java.util.Map;

import org.jxstar.util.config.SystemConfig;
import org.jxstar.util.log.Log;


/**
 * 系统工厂对象，负责创建系统级的对象。
 * 根据对象接口的名称(不含包名), 从配置文件中找到具体的类名(含包名), 创建具体对象.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings("rawtypes")
public class SystemFactory {
	//日志对象
	private static Log _log = Log.getInstance();	
	//系统对象池
	private static Map<String,Object> _mpSystem = FactoryUtil.newMap();

	/**
	 * 创建系统级的对象。
	 * 
	 * @param sObjName - 对象接口名称,将根据该标示从系统配置文件去具体类名
	 * @return Object
	 */
	public static Object createSystemObject(String sObjName) {
		String sClassName = findClassName(sObjName);
		if (sClassName == null || sClassName.length() == 0) {
			return null;
		}
		
		//创建单例对象
		synchronized(_mpSystem) {
			Object ret = _mpSystem.get(sObjName);
			if (ret != null) {
				return ret;
			}
			
			Class clazz = null;
			try {
				clazz = Class.forName(sClassName);
			} catch (ClassNotFoundException e) {
				_log.showError(e);
				return ret;
			}
			try {
				ret = clazz.newInstance();
			} catch (InstantiationException e) {
				_log.showError(e);
				return ret;
			} catch (IllegalAccessException e) {
				_log.showError(e);
				return ret;
			}
			
			_mpSystem.put(sObjName, ret);
			return ret;			
		}
	}
	
	/**
	 * 创建普通对象。
	 * 
	 * @param sClassName 类全名，含包名
	 * @return
	 */
	public static Object createObject(String sClassName) {
		Object ret = null;
		if (sClassName == null || sClassName.length() == 0) {
			return ret;
		}
		
		Class clazz = null;
		try {
			clazz = Class.forName(sClassName);
		} catch (ClassNotFoundException e) {
			_log.showError(e);
			return ret;
		}
		try {
			ret = clazz.newInstance();
		} catch (InstantiationException e) {
			_log.showError(e);
			return ret;
		} catch (IllegalAccessException e) {
			_log.showError(e);
			return ret;
		}
		
		return ret;
	}
    
    /**
     * 根据接口名称从配置文件中返回对象类名与包名
     * @param flag - 接口名称
     * @return String 返回对象类名与包名
     */
	private static String findClassName(String flag) {
		return SystemConfig.getConfigByKey("SystemObjects", flag);
	}
}
