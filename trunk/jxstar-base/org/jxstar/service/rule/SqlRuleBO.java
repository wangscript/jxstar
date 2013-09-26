/*
 * SqlRuleBO.java 2009-12-12
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.rule;

import java.util.List;
import java.util.Map;

import org.jxstar.service.BusinessObject;
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
		_log.showDebug("============sql rule routeId=" + routeId);
		
		//取导入SQL定义，如果没有定义则不处理
		Map<String, String> mpRule = _ruleUtil.queryRule(routeId, destFunId);
		if (mpRule.isEmpty()) {
			setMessage(JsMessage.getValue("sqlrulebo.mainnorule"), destFunId);
			return _returnFaild;
		}
		
		//取除第一条SQL外的语句
		List<Map<String, String>> lsOther = _ruleUtil.queryOtherRule(routeId);
		_log.showDebug("============other sql size=" + lsOther.size());
		
		//保存新的记录ID返回前台
		StringBuilder sbkeyid = new StringBuilder();
		
		//执行数据导入
		for (int i = 0, n = selKeyId.length; i < n; i++) {
			//执行主表数据导入，返回新增的记录主键ID
			String newKeyId = _ruleUtil.exeInsert(mpRule, selKeyId[i], forKeyId, userInfo);
			if (newKeyId.equals("false")) {
				setMessage(JsMessage.getValue("sqlrulebo.mainerror"), destFunId);
				return _returnFaild;
			}
			sbkeyid.append("{impKeyId:'"+ selKeyId[i] +"'"+", newKeyId:'"+ newKeyId +"'},");
			
			//如果没有其它反馈SQL，则不处理
			if (lsOther.isEmpty()) continue;
			
			_log.showDebug("============start execute import event other sql");
			//新的外键值，一般只有一条
			String[] newKeyIds = newKeyId.split(";");
			//继续执行其它反馈SQL
			for (int j = 0, m = newKeyIds.length; j < m; j++) {
				if (!_ruleUtil.exeUpdate(lsOther, selKeyId[i], newKeyIds[j], userInfo)) {
					setMessage(JsMessage.getValue("sqlrulebo.updateerror"));
					return _returnFaild;
				}
			}
			_log.showDebug("============end execute import event other sql\r\n");
		}
		//把新增主键值返回到前台
		String json = "[]";
		if (sbkeyid.length() > 0) {
			json = "[" + sbkeyid.substring(0, sbkeyid.length()-1) + "]";
		}
		setReturnData(json);
		_log.showDebug("------------sql rule import return data: " + json);
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
			setMessage(JsMessage.getValue("common.paramerror"));
			return _returnFaild;
		}
		_log.showDebug("------------sql rule update param funid="+funId+" eventcode="+eventCode+" selkeyid="+ArrayUtil.arrayToString(selKeyId));
		
		List<Map<String, String>> lsRule = _ruleUtil.queryUpdateRule(funId, eventCode);
		if (lsRule.isEmpty()) {
			_log.showDebug("------------not rule define update sql!");
			return _returnSuccess; 
		}
		
		for (int i = 0, n = selKeyId.length; i < n; i++) {
			if (!_ruleUtil.exeUpdate(lsRule, selKeyId[i], "", userInfo)) {
				setMessage(JsMessage.getValue("sqlrulebo.updateerror"));
				return _returnFaild;
			}
		}
		_log.showDebug("------------sql rule update end.");
		
		return _returnSuccess;
	}
}
