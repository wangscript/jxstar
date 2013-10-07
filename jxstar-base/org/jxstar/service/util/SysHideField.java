/*
 * Copyright(c) 2013 Donghong Inc.
 */
package org.jxstar.service.util;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.StringUtil;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 查询用户关于某个功能的隐藏数据的字段信息。
 * 方案1：
 * 如果有多个角色，只要其中某个角色没有设置隐藏数据的字段，则不处理隐藏字段。
 * 必须此用户的所有角色都设置需要隐藏的字段才有效。
 * 方案2：
 * 取此用户所有角色针对此功能设置隐藏字段的集合，作为需要控制的隐藏字段。
 *
 * @author TonyTan
 * @version 1.0, 2013-7-5
 */
public class SysHideField extends BusinessObject {
	private static final long serialVersionUID = 1L;
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 取该用户指定功能的需要隐藏数据的字段名。
	 * @param userId
	 * @param funId
	 * @return
	 */
	public static List<String> getHideCols(String userId, String funId) {
		String sql = "select distinct c.col_code from sys_user_role a, sys_role_fun b, sys_role_field c where " +  
				"a.role_id = b.role_id and b.role_fun_id = c.role_fun_id and a.user_id = ? and b.fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		param.addStringValue(funId);
		
		List<String> lsRet = FactoryUtil.newList();
		List<Map<String,String>> lsCol = _dao.query(param);
		if (lsCol.isEmpty()) {
			return lsRet;
		}
		
		for (Map<String,String> mpCol : lsCol) {
			String col_code = mpCol.get("col_code");
			//取不带表名的字段名
			col_code = StringUtil.getNoTableCol(col_code);
			lsRet.add(col_code);
		}
		
		return lsRet;
	}
	
	/**
	 * 取该用户指定功能的需要隐藏数据的字段名。
	 * @param userId -- 用户ID
	 * @param funId -- 功能ID
	 * @return List<String>
	 * @deprecated 暂时不用此方案1，用getHideCols替代。
	 */
	public static List<String> getOneHideCols(String userId, String funId) {
		List<String> lsRet = FactoryUtil.newList();
		
		//查询拥有此功能的角色
		List<String> lsRole = hasFunRoles(userId, funId);
		for (String roleId : lsRole) {
			lsRet = getRoleHideCols(funId, roleId);
			//如果存在没有设置隐藏字段的角色，则返回空
			if (lsRet.isEmpty()) {
				return lsRet;
			}
		}
		
		return lsRet;
	}
	
	//执行指定角色的隐藏字段设置
	private static List<String> getRoleHideCols(String funId, String roleId) {
		String sql = "select a.col_code from sys_role_field a, sys_role_fun b where " +
				"a.role_fun_id = b.role_fun_id and b.fun_id = ? and b.role_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.addStringValue(roleId);
		
		List<String> lsRet = FactoryUtil.newList();
		List<Map<String,String>> lsCol = _dao.query(param);
		if (lsCol.isEmpty()) {
			return lsRet;
		}
		
		for (Map<String,String> mpCol : lsCol) {
			String col_code = mpCol.get("col_code");
			col_code = StringUtil.getNoTableCol(col_code);
			lsRet.add(col_code);
		}
		
		return lsRet;
	}
	
	//查询拥有此功能的角色
	private static List<String> hasFunRoles(String userId, String funId) {
		String sql = "select distinct a.role_id from sys_user_role a, sys_role_fun b where " + 
				"a.role_id = b.role_id and a.user_id = ? and b.fun_id = ?";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		param.addStringValue(funId);
		
		List<String> lsRet = FactoryUtil.newList();
		List<Map<String,String>> lsRole = _dao.query(param);
		if (lsRole.isEmpty()) {
			return lsRet;
		}
		
		for (Map<String,String> mpRole : lsRole) {
			String roleId = mpRole.get("role_id");
			lsRet.add(roleId);
		}
		
		return lsRet;
	}
}
