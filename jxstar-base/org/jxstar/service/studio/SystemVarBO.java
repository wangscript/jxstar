/*
 * SystemVarBO.java 2011-4-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.studio;

import org.jxstar.service.BusinessObject;
import org.jxstar.util.config.SystemVar;

/**
 * 通过系统属性功能动态修改属性值，当前方法处理不严谨，
 * 没有考虑变量编码修改与删除的情况，但不影响使用，因为所有新的变量都会加载，
 * 且系统重启后，所有变量会重新加载。
 *
 * @author TonyTan
 * @version 1.0, 2011-4-1
 */
public class SystemVarBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 保存后的方法，如果有多条记录，则值格式如：var1,var2,var3...
	 * @param keys -- 修改的变量代号
	 * @param values -- 修改的变量值
	 * @return
	 */
	public String postSave(String[] keys, String[] values) {
		//不需判断values，可能存在空值
		if (keys == null || keys.length == 0) {
			_log.showDebug("没有系统属性被修改！");
			return _returnSuccess;
		}
		
		for (int i = 0, n = keys.length; i < n; i++) {
			String key = keys[i];
			String value = values[i];
			
			_log.showDebug("动态修改【{0}】系统属性的值为【{1}】！", key, value);
			SystemVar.setValue(key, value);
		}
		
		return _returnSuccess;
	}
}
