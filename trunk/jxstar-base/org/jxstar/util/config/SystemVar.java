/*
 * SystemVar.java 2010-11-4
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.util.config;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 系统属性配置表，系统启动时从系统配置表中加载配置，支持动态修改配置。
 * 
 * 在SystemPropertyBO类中可以动态更新系统配置，而不用重启系统。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-4
 */
public class SystemVar {
	//缓存系统所有配置信息
	private static Map<String,String> _mpVar = FactoryUtil.newMap();
	//系统程序路径，在ActionHelper中赋值，如：D:/Tomcat6/webapps/jxstar
	public static String REALPATH = "";

	/**
	 * 取系统配置属性
	 * @param keyName -- 属性名称
	 * @return
	 */
	public static String getValue(String keyName) {
		return getValue(keyName, "");
	}
	
	/**
	 * 动态修改系统配置值
	 * @param keyName -- 参数名称
	 * @param keyValue -- 参数值
	 */
	public static void setValue(String keyName, String keyValue) {
		if (keyName == null || keyName.length() == 0) return;
		
		if (keyValue == null) keyValue = "";
		
		_mpVar.put(keyName, keyValue);
	}
	
	/**
	 * 取系统配置属性，如果属性值为空，则返回指定的缺省值
	 * @param keyName -- 属性名称
	 * @param defaultValue -- 缺省值
	 * @return 
	 */
	public static String getValue(String keyName, String defaultValue) {
		if (keyName == null || keyName.length() == 0) return "";
		
		String value = _mpVar.get(keyName.toLowerCase());
		
		if (value == null || value.length() == 0) {
			value = defaultValue;
		}
		
		return value;
	}
	
	/**
	 * 系统启动时，加载所有配置信息。
	 *
	 */
	public static void init() {
		BaseDao _dao = BaseDao.getInstance();
		
		String sql = "select var_code, var_value from sys_var";
		DaoParam param = _dao.createParam(sql);
		List<Map<String,String>> lsVar = _dao.query(param);
		
		for (int i = 0, n = lsVar.size(); i < n; i++) {
			Map<String,String> mpVar = lsVar.get(i);
			
			String key = mpVar.get("var_code");
			String value = mpVar.get("var_value");
			_mpVar.put(key, value);
		}
	}
	
	/**
	 * 把用于页面的系统变量转化为json对象。
	 * @return
	 */
	public static String getVarJs() {
		BaseDao _dao = BaseDao.getInstance();
		
		String sql = "select var_code, var_value from sys_var where use_page = '1'";
		DaoParam param = _dao.createParam(sql);
		List<Map<String,String>> lsVar = _dao.query(param);
		
		StringBuilder sbJs = new StringBuilder();
		for (Map<String,String> mpVar : lsVar) {
			String key = mpVar.get("var_code");
			String value = mpVar.get("var_value");
			
			key = key.replaceAll("\\.", "__");
			value = StringUtil.strForJson(value);
			
			sbJs.append("'" + key + "':").append("'" + value + "',");
		}
		
		String json = "{}";
		if (sbJs.length() > 0) {
			json = "{" + sbJs.substring(0, sbJs.length()-1) + "}";
		}
		return json;
	}
}
