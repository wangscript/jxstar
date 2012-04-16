/*
 * WarnUtil.java 2011-3-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */

package org.jxstar.service.warn;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dao.DmDaoUtil;
import org.jxstar.service.util.ConditionUtil;
import org.jxstar.service.util.SysDataManager;
import org.jxstar.service.util.WhereUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 上报事务处理的工具类，由于上报组件执行频率比较高，对性能要求比较高。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-17
 */
public class WarnUtil {
	//日志对象
	private static Log _log = Log.getInstance();
	//dao对象
	private static BaseDao _dao = BaseDao.getInstance();
	//上报定义表的字段
	private static String _warn_field = DmDaoUtil.getFieldSql("warn_base");
	//用户表的字段
	private static String _user_field = DmDaoUtil.getFieldSql("sys_user");

	/**
	 * 查询所有生效的上报注册记录
	 * @return
	 */
	public static List<Map<String,String>> queryWarn() {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select ");
		sbsql.append(_warn_field);
		sbsql.append(" from warn_base where run_state = '2'");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		return _dao.query(param);
	}
	
	/**
	 * 查询符合上报条件的业务记录，不含已生成上报消息的记录
	 * @param funId -- 功能ID
	 * @param tableName -- 功能表名
	 * @param keyName -- 功能主键字段
	 * @param warnId -- 上报ID
	 * @param whereSql -- 上报条件
	 * @param timeValue -- 判断间隔时间值
	 * @return
	 */
	public static List<Map<String,String>> queryData(String funId, 
			String tableName, String keyName, String warnId, 
			String whereSql, String timeValue) {
		whereSql = whereSql.trim();
		//取功能定义的where+归档处理where
		String baseWhere = WhereUtil.queryBaseWhere(funId);
		
		StringBuilder sbsql = new StringBuilder();
		//拼接查询sql
		sbsql.append("select * from ").append(tableName).append(" where ");
		
		if (baseWhere.length() > 0) {
			sbsql.append("(").append(baseWhere).append(") and ");
		}
		
		//如果已经有上报消息的记录，则不处理
		keyName = tableName + "." + keyName;
		sbsql.append("not exists (select * from warn_assign where fun_id = ? and warn_id = ? and data_id = ").append(keyName).append(")");
		
		if (whereSql.length() > 0) {
			sbsql.append(" and (").append(whereSql).append(")");
		}
		_log.showDebug("query warn sql is: " + sbsql);
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.setUseParse(true);
		
		//设置上报定义参数
		param.addStringValue(funId);
		param.addStringValue(warnId);
		
		//如果设置间隔时间值
		if (timeValue.length() > 0) {
			if (whereSql.indexOf("?") > 0) {
				Calendar cal = intervalDate(Calendar.getInstance(), timeValue);
				String date = DateUtil.calendarToDateTime(cal);
				param.addDateValue(date);
			}
		}
		
		return _dao.query(param);
	}
	
	/**
	 * 从系统权限定义查找符合条件的用户：
	 * 用户必须拥有该功能的编辑权限；用户必须用户该功能的数据权限 
	 * @param funId -- 功能ID
	 * @param mpData -- 业务数据
	 * @return
	 */
	public static List<Map<String,String>> queryRoleUser(String funId, Map<String,String> mpData) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		//查找拥有指定功能编辑权限的用户
		List<Map<String,String>> lsUser = queryEditUser(funId);
		if (lsUser.isEmpty())  return lsRet;
		
		//数据权限解析SQL缓存管理对象
		SysDataManager manager = SysDataManager.getInstance();
		
		for (int i = 0, n = lsUser.size(); i < n; i++) {
			Map<String,String> mpUser = lsUser.get(i);
			
			String userId = mpUser.get("user_id");
			//取数据权限判断SQL
			String datasql = manager.getDataWhere(userId, funId);
			//把数据权限SQL中的字段去掉表名并添加[]
			datasql = parseDataWhere(datasql);
			
			//添加到条件表达式中
			mpUser.put("cond_where", datasql);
			
			//添加有数据权限的用户
			if (ConditionUtil.parseCondition(mpUser, mpData)) {
				lsRet.add(mpUser);
			}
		}
		
		return lsRet;
	}
	
	/**
	 * 根据上报定义中的通知用户明细，查找符合条件的用户
	 * @param warnId -- 上报定义ID
	 * @param mpData -- 应用数据
	 * @return
	 */
	public static List<Map<String,String>> queryWarnUser(String warnId, Map<String,String> mpData) {
		List<Map<String,String>> lsRet = FactoryUtil.newList();
		//查找上报设置的通知用户明细
		List<Map<String,String>> lsUser = queryDetUser(warnId);
		if (lsUser.isEmpty())  return lsRet;
		
		for (int i = 0, n = lsUser.size(); i < n; i++) {
			Map<String,String> mpUser = lsUser.get(i);
			
			//解析判断条件，取符合条件的用户
			if (ConditionUtil.parseCondition(mpUser, mpData)) {
				lsRet.add(mpUser);
			}
		}
		
		return lsRet;
	}
	
	/**
	 * 给日历值添加间隔值，可以是负值
	 * @param cal -- 日历值
	 * @param interval -- 间隔值
	 * @return
	 */
	public static Calendar intervalDate(Calendar cal, String interval) {
		//解析间隔时间值
		int len = interval.length();
		char unit = interval.charAt(len-1);
		int value = Integer.parseInt(interval.substring(0, len-1));
		
		//计算目标时间
		if (unit == 'm') {
			cal.add(Calendar.MINUTE, value);
		} else if (unit == 'h') {
			cal.add(Calendar.HOUR_OF_DAY, value);
		} else if (unit == 'd') {
			cal.add(Calendar.DAY_OF_MONTH, value);
		}
		
		return cal;
	}
	
	/**
	 * 查询上报配置通知用户明细
	 * @param warnId -- 上报用户明细
	 * @return
	 */
	private static List<Map<String,String>> queryDetUser(String warnId) {
		String sql = "select * from warn_user where warn_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(warnId);
		
		return _dao.query(param);
	}
	
	/**
	 * 查找拥有指定功能编辑权限的用户，不考虑管理角色用户
	 * @param funId -- 功能ID
	 * @return
	 */
	private static List<Map<String,String>> queryEditUser(String funId) {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select "+ _user_field +" from sys_user where exists ");
		sbsql.append("(select * from sys_user_role, sys_role_fun ");
		sbsql.append("where sys_user_role.role_id = sys_role_fun.role_id and sys_role_fun.is_edit = '1' and ");
		sbsql.append("sys_user_role.user_id = sys_user.user_id and sys_role_fun.fun_id = ?)");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(funId);
		return _dao.query(param);
	}
	
	/**
	 * 给数据权限where中的字段名添加[]，并去掉表名，处理方法是：
	 * 先根据“ ”空格分隔字符串，再查到有.的短语就是字段名
	 * @param where -- 数据权限where
	 * @return
	 */
	public static String parseDataWhere(String where) {
		if (where == null || where.length() == 0) return "";
		
		String[] wheres = where.split(" ");
		
		StringBuilder sbwhere = new StringBuilder();
		for (int i = 0, n = wheres.length; i < n; i++) {
			String phrase = wheres[i];
			if (phrase.indexOf(".") > 0) {
				String[] fields = phrase.split("\\.");
				
				if (fields[0].charAt(0) == '(') {
					sbwhere.append('(');
				}
				sbwhere.append('[').append(fields[1]).append("] ");
			} else {
				sbwhere.append(phrase).append(' ');
			}
		}
		
		return sbwhere.toString();
	}
}
