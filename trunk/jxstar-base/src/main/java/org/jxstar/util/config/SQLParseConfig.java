/*
 * SQLParseConfig.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;

/**
 * SQL语句中的函数解析的配置信息.
 * 系统初始化时将给该对象赋初始值.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings("unchecked")
public class SQLParseConfig {
	//配置值对象
	private static Map<String,Object> _mpConfig = FactoryUtil.newMap();
	
	public static void init(Map<String,Object> mpConfig) {
		_mpConfig = mpConfig;
	}
	
	public static Map<String,Object> getConfigMap() {
		return _mpConfig;
	}
	
	/**
	 * 返回该函数标示的配置信息.
	 * 
	 * @param sFunIdent - 函数标示
	 * @return Map - 函数标示的配置信息
	 */
	public static Map<String,Object> getFunConfig(String sFunIdent) {
		return (Map<String,Object>) _mpConfig.get(sFunIdent);
	}
	
	/**
	 * 返回函数名及参数情况.
	 * 
	 * @param sFunIdent	- 函数标示
	 * @param sDBType	- 数据库类型
	 * @return String
	 */
	public static String getFunName(String sFunIdent, String sDBType) {
		String sRet = "";
		if (_mpConfig == null || _mpConfig.isEmpty()) {
			return sRet;
		}
		
		Map<String,Object> mpTmp = (Map<String,Object>) _mpConfig.get(sFunIdent);
		if (mpTmp == null || mpTmp.isEmpty()) {
			return sRet;
		}
		
		sRet = (String) mpTmp.get(sDBType);
		return (sRet == null) ? "" : sRet;
	}
	
	/**
	 * 返回该函数是否要解析参数.
	 * 
	 * @param sFunIdent	- 函数标示
	 * @return boolean 
	 */
	public static boolean isParseParam(String sFunIdent) {
		String sRet = "";
		if (_mpConfig == null || _mpConfig.isEmpty()) {
			return false;
		}
		
		Map<String,Object> mpTmp = (Map<String,Object>) _mpConfig.get(sFunIdent);
		if (mpTmp == null || mpTmp.isEmpty()) {
			return false;
		}
		
		sRet = (String) mpTmp.get("isparse-param");
		if (sRet == null) return true;
		
		return (! sRet.toLowerCase().equals("0"));
	}
}
