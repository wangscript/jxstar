/*
 * SystemConfig.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.List;
import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;

/**
 * 框架配置文件对象,系统初始化时将给该对象赋初始值.
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
@SuppressWarnings("unchecked")
public class SystemConfig {
	//配置值对象
	private static Map<String,Object> _mpConfig = FactoryUtil.newMap();
	
	public static void init(Map<String,Object> mpConfig) {
		_mpConfig = mpConfig;
	}
	
	public static Map<String,Object> getConfigMap() {
		return _mpConfig;
	}	
	
	/**
	 * 取一节点中的值。
	 * @param sNodeName	-- 一级节点的名称
	 * @param sKeyName	-- 节点中的参数值
	 * @return String
	 */
	public static String getConfigByKey(String sNodeName, String sKeyName) {
		String sRet = "";
		if (_mpConfig == null || _mpConfig.isEmpty()) {
			return sRet;
		}
		
		Map<String,String> mpTmp = (Map<String,String>) _mpConfig.get(sNodeName);
		if (mpTmp == null || mpTmp.isEmpty()) {
			return sRet;
		}
		
		sRet = mpTmp.get(sKeyName);
		return (sRet == null) ? "" : sRet;
	}
	
	/**
	 * 取一节点中的值，为List对象。
	 * @param sNodeName	-- 一级节点的名称
	 * @return List 元素为Map对象
	 */
	public static List<Map<String,String>> getConfigListByKey(String sNodeName) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		if (_mpConfig == null || _mpConfig.isEmpty()) {
			return lsRet;
		}
		
		List<Map<String,String>> lsTemp = 
			(List<Map<String,String>>) _mpConfig.get(sNodeName); 
		return (lsTemp == null) ? lsRet : lsTemp;
	}
}
