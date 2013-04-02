/*
 * SysUserUtil.java 2008-5-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.util;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.util.MapUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 用户权限工具类.
 * 
 * @author TonyTan
 * @version 1.0, 2008-5-18
 */
public class SysUserUtil {
	//数据库访问对象
	private static BaseDao _dao = BaseDao.getInstance();

	/**
	 * 判断用户是否有该功能的权限.
	 * 
	 * @param sFunID - 功能ID
	 * @param sUserID - 用户ID
	 * @param sRoleID - 角色ID
	 * @return boolean
	 */
	public static boolean hasRight(String sFunID, String sUserID, 
			String sRoleID) {
		if (sUserID == null || sFunID == null) return false;
		//超级管理员有所以功能的权限
		if (sUserID.equals("admin")) {
			return true;
		}
		//管理员角色有所以功能的权限
		if (sRoleID != null && sRoleID.equals("administrator")) {
			return true;
		}
		
		StringBuilder sb = new StringBuilder("");
			sb.append("select count(*) as cnt ");
			sb.append("from sys_user_role, sys_role_fun ");
			sb.append("where sys_user_role.role_id = sys_role_fun.role_id ");
			sb.append("and sys_user_role.user_id = ? ");
			sb.append("and sys_role_fun.fun_id = ? ");
			
		DaoParam param = _dao.createParam(sb.toString());
		param.addStringValue(sUserID).addStringValue(sFunID);
		Map<String,String> mp = _dao.queryMap(param);
			
		return MapUtil.hasRecord(mp);
	}
	
	/**
	 * 根据账号取用户信息.
	 * 
	 * @param sUserCode - 登陆账号
	 * @return Map
	 */
	public static Map<String,String> getUserByCode(String sUserCode) {
		StringBuilder sb = new StringBuilder("select ");
			sb.append("sys_user.user_id, sys_user.user_code, sys_user.user_name, ");
			sb.append("sys_user.user_pwd, ");
			sb.append("sys_dept.dept_id, sys_dept.dept_code, sys_dept.dept_name ");
			sb.append("from sys_user, sys_dept ");
			sb.append("where sys_user.dept_id = sys_dept.dept_id ");
			sb.append("and sys_dept.is_novalid = '0'");
			sb.append("and sys_user.is_novalid = '0'");
			sb.append("and sys_user.user_code = ?");
		
		DaoParam param = _dao.createParam(sb.toString())
			.addStringValue(sUserCode);
		return _dao.queryMap(param);
	}
	
	/**
	 * 根据用户ID取用户信息.
	 * 
	 * @param userId - 用户ID
	 * @return Map
	 */
	public static Map<String,String> getUserById(String userId) {
		StringBuilder sb = new StringBuilder("select ");
			sb.append("sys_user.user_id, sys_user.user_code, sys_user.user_name, ");
			sb.append("sys_dept.dept_id, sys_dept.dept_code, sys_dept.dept_name ");
			sb.append("from sys_user, sys_dept ");
			sb.append("where sys_user.dept_id = sys_dept.dept_id ");
			sb.append("and sys_dept.is_novalid = '0'");
			sb.append("and sys_user.is_novalid = '0'");
			sb.append("and sys_user.user_id = ?");
		
		DaoParam param = _dao.createParam(sb.toString())
			.addStringValue(userId);
		return _dao.queryMap(param);
	}
	
	/**
	 * 判断用户是否为系统管理员
	 * @param sUserID
	 * @return
	 */
	public static boolean isAdmin(String sUserID) {
		String sql = "select count(*) as cnt from sys_user_role where role_id = ? and user_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue("administrator");
		param.addStringValue(sUserID);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.hasRecord(mp);
	}
	
	/**
	 * 取该用户的角色ID.
	 * 
	 * @param sUserID - 用户ID
	 * @return String
	 */
	public static String getRoleID(String sUserID) {
		String sql = "select role_id from sys_user_role where user_id = ?";
		
		DaoParam param = _dao.createParam(sql)
			.addStringValue(sUserID);
		Map<String,String> mp = _dao.queryMap(param);
		if (mp == null || mp.isEmpty()) {
			return "";
		}
		return (String) mp.get("role_id");
	}
	
	/**
	 * 取功能的事件控制权限，返回参数有：query, edit, print, audit, other
	 * 如果有对应的操作权限，则参数值为1，否则为0.
	 * 
	 * @param sUserID -- 用户ID
	 * @param sFunID -- 功能ID
	 * @return
	 */
	public static Map<String,String> getFunRight(String sUserID, String sFunID) {
		Map<String,String> mpRight = FactoryUtil.newMap();
		mpRight.put("query", "1");
		mpRight.put("edit", "0");
		mpRight.put("print", "0");
		mpRight.put("audit", "0");
		mpRight.put("other", "0");
		
		StringBuilder sbsql = new StringBuilder("select sys_role_fun.is_edit, sys_role_fun.is_print, ");
		sbsql.append("sys_role_fun.is_audit, sys_role_fun.is_other ");
		sbsql.append("from sys_user_role, sys_role_fun where ");
		sbsql.append("sys_user_role.role_id = sys_role_fun.role_id and ");
		sbsql.append("sys_user_role.user_id = ? and sys_role_fun.fun_id = ?");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(sUserID);
		param.addStringValue(sFunID);
		
		List<Map<String,String>> lsFun = _dao.query(param);
		if (lsFun.isEmpty()) return mpRight;
		
		for (int i = 0, n = lsFun.size(); i < n; i++) {
			Map<String,String> mp = lsFun.get(i);
			
			if (mp.get("is_edit").equals("1")) mpRight.put("edit", "1") ;
			if (mp.get("is_print").equals("1")) mpRight.put("print", "1") ;
			if (mp.get("is_audit").equals("1")) mpRight.put("audit", "1") ;
			if (mp.get("is_other").equals("1")) mpRight.put("other", "1") ;
		}
		
		return mpRight;
	}
	
	/**
	 * 查询角色用户的用户
	 * @param roleId -- 角色ID
	 * @return
	 */
	public static List<Map<String,String>> queryRoleUser(String roleId) {
		String sql = "select * from sys_user where exists (select * from sys_user_role " +
					 "where sys_user_role.user_id = sys_user.user_id and sys_user_role.role_id = ?)";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(roleId);
		
		return _dao.query(param);
	}
}
