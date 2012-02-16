/*
 * PortletFunBO.java 2010-12-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.portlet;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;

/**
 * 常用功能PORTLET处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-31
 */
public class PortletFunBO extends BusinessObject {
	private static final long serialVersionUID = -8412526644118433040L;

	/**
	 * 取常用功能信息的JSON各前台PortletFun.js对象
	 * @param portletId
	 * @return
	 */
	public String getFunJson(String portletId) {
		List<Map<String,String>> lsFun = queryPletFun(portletId);
		if (lsFun.isEmpty()) {
			setReturnData("[]");
			return _returnSuccess;
		}
		
		//构建功能列表的JSON
		StringBuilder sbfuns = new StringBuilder();
		for (int i = 0, n = lsFun.size(); i < n; i++) {
			Map<String,String> mpFun = lsFun.get(i);
			
			sbfuns.append("{funid:'"+ mpFun.get("fun_id") +"',");
			sbfuns.append("funname:'"+ mpFun.get("fun_name") +"'}");
			sbfuns.append((i < n - 1) ? ",\n" : "\n");
		}
		
		String funJson = "["+ sbfuns.toString() +"]";
		_log.showDebug("---------funJson=" + funJson);
		setReturnData(funJson);
		
		return _returnSuccess;
	}
	
	/**
	 * 取模板栏目的常用功能列表
	 * @param portletId -- 栏目ID
	 * @return
	 */
	private List<Map<String,String>> queryPletFun(String portletId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select fun_id, fun_name from plet_fun ");
		sql.append("where portlet_id = ? order by fun_no ");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(portletId);
		return _dao.query(param);
	}
}
