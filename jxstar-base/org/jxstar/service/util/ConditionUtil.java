/*
 * ConditionUtil.java 2011-1-29
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.MapUtil;
import org.jxstar.util.log.Log;

/**
 * 判断条件解析类，判断条件是SQL语句，编辑规则是：
 * {field_name} = 'value' and {dept_id} = '{CURUSERID}' and ...
 * 字段必须是业务表中的字段；
 * 值可以是常量值，或者会话对象中的常量值，
 * 如当前用户ID {CURUSERID}、当前部门ID {CURDEPTID}。
 * 
 * 主要用于工作流的条件解析与上报组件
 *
 * @author TonyTan
 * @version 1.0, 2011-1-29
 */
public class ConditionUtil {
	private static Log _log = Log.getInstance();
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 解析流转的判断条件，检查是否符合条件。
	 * @param processId -- 过程ID
	 * @param lineId -- 流转ID
	 * @param appData -- 应用数据
	 * @return
	 */
	public static boolean parseCondition(String processId, 
			String lineId, Map<String,String> appData) {
		String condition = getLineCondition(processId, lineId);
		if (condition == null || condition.length() == 0) return true;
		
		//解析应用数据字段
		condition = TaskUtil.parseAppField(condition, appData, true);
		_log.showDebug("----------检查符合条件的路径SQL=" + condition);
		
		return validCondition(condition);
	}

	/**
	 * 解析分配用户定义信息，检查当前用户是否满足条件。
	 * @param mpDefine -- 用户分配信息
	 * @param appData -- 应用数据
	 * @return
	 */
	public static boolean parseCondition(Map<String,String> mpDefine, Map<String,String> appData) {
		String condition = mpDefine.get("cond_where");
		if (condition == null || condition.length() == 0) return true;
		
		//如果条件中有用户常量值
		if (condition.indexOf("{CUR") >= 0) {
			//取分配的用户ID
			String userId = mpDefine.get("user_id");
			//取用户信息
			Map<String,String> mpUser = SysUserUtil.getUserById(userId);
			//解析用户常量
			condition = parseConstant(condition, mpUser);
		}
		
		//解析应用数据字段
		condition = TaskUtil.parseAppField(condition, appData, true);
		_log.showDebug("----------检查符合条件的用户SQL=" + condition);
		
		return validCondition(condition);
	}
	
	/**
	 * 检查查询条件SQL语句是否合法
	 * @param condition -- 查询条件SQL
	 * @return
	 */
	public static boolean validCondition(String condition) {
		if (condition == null || condition.length() == 0) return true;
		
		String sql = "select count(*) as cnt from fun_base where fun_id = 'sys_fun_base' and (" + condition + ")";
		DaoParam param = _dao.createParam(sql);
		
		return MapUtil.hasRecord(_dao.queryMap(param));
	}
	
	/**
	 * 解析条件中的用户常量值。
	 * @param condition -- 判断条件
	 * @param mpUser -- 用户信息
	 * @return
	 */
	private static String parseConstant(String condition, Map<String,String> mpUser) {
		if (condition == null || condition.length() == 0) return "";
		if (mpUser == null || mpUser.isEmpty()) return condition;
		
		condition = condition.replaceAll("{CURUSERID}", mpUser.get("user_id"));
		
		condition = condition.replaceAll("{CURUSERCODE}", mpUser.get("user_code"));
		
		condition = condition.replaceAll("{CURUSERNAME}", mpUser.get("user_name"));
		
		condition = condition.replaceAll("{CURDEPTID}", mpUser.get("dept_id"));
		
		condition = condition.replaceAll("{CURDEPTCODE}", mpUser.get("dept_code"));
		
		condition = condition.replaceAll("{CURDEPTNAME}", mpUser.get("dept_name"));
		
		return condition;
	}
	
	/**
	 * 取流转的判断条件。
	 * @param processId -- 过程ID
	 * @param lineId -- 流转ID
	 * @return
	 */
	private static String getLineCondition(String processId, String lineId) {
		String sql = "select cond_where from wf_condition where process_id = ? and line_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(processId);
		param.addStringValue(lineId);
		
		Map<String,String> mpData = _dao.queryMap(param);
		if (mpData.isEmpty()) return "";
		
		return mpData.get("cond_where");
	}
}
