/*
 * SqlRuleBO.java 2009-12-12
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.rule;

import java.util.Map;


import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineDataManger;
import org.jxstar.util.ArrayUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * SQL规则执行事件。
 *
 * @author TonyTan
 * @version 1.0, 2009-12-12
 */
public class SqlRuleBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	//规则解析工具类
	private RuleUtil _ruleUtil = new RuleUtil();

	/**
	 * 执行数据导入事件的SQL规则定义。
	 * @param srcFunId -- 来源功能ID
	 * @param destFunId -- 目标功能ID
	 * @param selKeyId -- 选择的记录ID
	 * @param forKeyId -- 外键ID
	 * @param userInfo -- 当前用户信息
	 * @return
	 */
	public String exeImport(String srcFunId, String destFunId, 
			String[] selKeyId, String forKeyId, Map<String,String> userInfo) {
		if (srcFunId == null || srcFunId.length() == 0 ||
				destFunId == null || srcFunId.length() == 0 ||
				selKeyId == null || selKeyId.length == 0) {
			setMessage(JsMessage.getValue("common.paramerror"));
			return _returnFaild;
		}
		
		if (forKeyId == null) forKeyId = "";
		_log.showDebug("------------sql rule import param srcfunid="+srcFunId+" destfunid="+
				destFunId + " forKeyId=" + forKeyId +" selkeyid="+
				ArrayUtil.arrayToString(selKeyId));
		
		//路由条件ID
		String routeId = _ruleUtil.queryRoute(srcFunId, destFunId);
		if (routeId.length() == 0) {
			setMessage(JsMessage.getValue("sqlrulebo.routenull"));
			return _returnFaild;
		}
		
		//目标功能的子功能ID
		DefineDataManger manger = DefineDataManger.getInstance();
		Map<String,String> destDefine = manger.getFunData(destFunId);
		String[] subFunIds = destDefine.get("subfun_id").split("/,");
		
		//执行数据导入
		for (int i = 0, n = selKeyId.length; i < n; i++) {
			//执行主表数据导入，返回新增的记录主键ID
			String newKeyId = _ruleUtil.exeInsert(srcFunId, destFunId, 
					selKeyId[i], forKeyId, routeId, userInfo);
			if (newKeyId.equals("true")) {
				setMessage(JsMessage.getValue("sqlrulebo.mainnorule"), destFunId);
				return _returnFaild;
			}
			if (newKeyId.equals("false")) {
				setMessage(JsMessage.getValue("sqlrulebo.mainerror"), destFunId);
				return _returnFaild;
			}
			
			//如果没有子功能，则不用处理
			if (subFunIds.length == 0) continue;
			
			//新的外键值
			String[] newKeyIds = newKeyId.split(";");
			//导入子表数据
			for (int j = 0, m = newKeyIds.length; j < m; j++) {
				for (int k = 0, p = subFunIds.length; k < p; k++) {
					//来源功能ID只是用来取数据源名，子表也采用父表的数据源
					String newSubId = _ruleUtil.exeInsert(srcFunId, subFunIds[k], 
						selKeyId[i], newKeyIds[j], routeId, userInfo);
					if (newSubId.equals("false")) {
						setMessage(JsMessage.getValue("sqlrulebo.suberror"), subFunIds[k]);
						return _returnFaild;
					}
				}
			}
		}
		_log.showDebug("------------sql rule import end.");
		
		return _returnSuccess;
	}
	
	/**
	 * 执行事件触发的SQL规则定义。
	 * @param funId -- 触发功能ID
	 * @param eventCode -- 事件编号
	 * @param selKeyId -- 选择的记录ID
	 * @param userInfo -- 当前用户信息
	 * @return
	 */
	public String exeUpdate(String funId, String eventCode, 
			String[] selKeyId, Map<String,String> userInfo) {
		if (funId == null || funId.length() == 0 ||
				selKeyId == null || selKeyId.length == 0) {
			setMessage(JsMessage.getValue("common.paramerror")  +"sdfs");
			return _returnFaild;
		}
		_log.showDebug("------------sql rule update param funid="+funId+" eventcode="+eventCode+" selkeyid="+ArrayUtil.arrayToString(selKeyId));
		
		for (int i = 0, n = selKeyId.length; i < n; i++) {
			if (!_ruleUtil.exeUpdate(funId, selKeyId[i], eventCode, userInfo)) {
				setMessage(JsMessage.getValue("sqlrulebo.updateerror"));
				return _returnFaild;
			}
		}
		_log.showDebug("------------sql rule update end.");
		
		return _returnSuccess;
	}
}
