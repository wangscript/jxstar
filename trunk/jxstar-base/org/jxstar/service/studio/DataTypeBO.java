/*
 * DataTypeBO.java 2010-11-22
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.studio;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.util.SysDataManager;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.DateUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.key.KeyCreator;
import org.jxstar.util.resource.JsMessage;

/**
 * 批量添加或删除数据权限处理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-22
 */
public class DataTypeBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 批量添加数据权限
	 * 
	 * @param keyids -- 选择的数据类型
	 * @param selfunid -- 选择的功能ID
	 * @param userId -- 当前用户ID
	 * @return
	 */
	public String addType(String[] keyids, String selfunid, String userId) {
		if (keyids == null || keyids.length == 0) {
			//"没有选择数据类型！"
			setMessage(JsMessage.getValue("datatypebo.nodatatype"));
			return _returnFaild;
		}
		
		if (selfunid == null || selfunid.length() == 0) {
			//"没有选择需要批量授权的功能ID！"
			setMessage(JsMessage.getValue("datatypebo.noselfunid"));
			return _returnFaild;
		}
		_log.showDebug("addType param selfunid=" + selfunid + " keyids.size=" + keyids.length);
		String[] selids = selfunid.split(",");
		
		//新增数据权限记录
		String isql = "insert into sys_role_data(role_data_id,role_fun_id,dtype_id,add_userid,add_date)" +
					  "values(?,?,?,?,?)";
		KeyCreator key = KeyCreator.getInstance();
		String curDate = DateUtil.getTodaySec();
		DaoParam param = _dao.createParam(isql);
		
		//检查如果存在的数据权限则不处理
		String ssql = "select count(*) as cnt from sys_role_data where dtype_id = ? and role_fun_id = ?";
		DaoParam sparam = _dao.createParam(ssql);
		
		for (int i = 0, n = keyids.length; i < n; i++) {
			for (int j = 0, m = selids.length; j < m; j++) {
				//如果存在就不新增
				sparam.addStringValue(keyids[i]);
				sparam.addStringValue(selids[j]);
				Map<String,String> mpCnt = _dao.queryMap(sparam);
				sparam.clearParam();
				if (MapUtil.hasRecord(mpCnt)) continue;
				
				//如果不存在就新增记录
				param.addStringValue(key.createKey("sys_role_data"));
				param.addStringValue(selids[j]);
				param.addStringValue(keyids[i]);
				param.addStringValue(userId);
				param.addDateValue(curDate);
				
				_dao.update(param);
				param.clearParam();
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 删除角色数据权限
	 * @param keyids -- 选择的数据类型
	 * @param selfunid -- 选择的功能ID
	 * @return
	 */
	public String delType(String[] keyids, String selfunid) {
		if (keyids == null || keyids.length == 0) {
			//"没有选择数据类型！"
			setMessage(JsMessage.getValue("datatypebo.nodatatype"));
			return _returnFaild;
		}
		
		if (selfunid == null || selfunid.length() == 0) {
			//"没有选择需要批量授权的功能ID！"
			setMessage(JsMessage.getValue("datatypebo.noselfunid"));
			return _returnFaild;
		}
		_log.showDebug("addType param selfunid=" + selfunid + " keyids.size=" + keyids.length);
		String[] selids = selfunid.split(",");
		String dsql = "delete from sys_role_data where dtype_id = ? and role_fun_id = ?";
		DaoParam param = _dao.createParam(dsql);
		
		for (int i = 0, n = keyids.length; i < n; i++) {
			for (int j = 0, m = selids.length; j < m; j++) {
				param.addStringValue(keyids[i]);
				param.addStringValue(selids[j]);
				
				_dao.update(param);
				param.clearParam();
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 当角色修改了数据权限时，需要清除相关的数据权限SQL缓存
	 * @param selfunid -- 选择的角色功能ID，用“,”分隔
	 * @return
	 */
	public String clearDataWhere(String selfunid) {
		if (selfunid == null || selfunid.length() == 0) {
			//"没有选择角色授权的功能ID！"
			setMessage(JsMessage.getValue("datatypebo.norolefunid"));
			return _returnFaild;
		}
		
		String[] selids = selfunid.split(",");
		Map<String,String> mpRoleFun = getRoleFun(selids[0]);
		if (mpRoleFun.isEmpty()) {
			//"角色授权的功能信息为空！"
			setMessage(JsMessage.getValue("datatypebo.nofuninfo"));
			return _returnFaild;
		}
		
		String roleId = mpRoleFun.get("role_id");
		List<Map<String,String>> lsUser = SysUserUtil.queryRoleUser(roleId);
		if (lsUser.isEmpty()) {
			//"当前角色没有注册用户，不用处理！"
			_log.showDebug(JsMessage.getValue("datatypebo.nouser"));
			return _returnSuccess;
		}
		
		//数据权限解析SQL缓存管理对象
		SysDataManager manager = SysDataManager.getInstance();
		
		for (int j = 0, m = selids.length; j < m; j++) {
			Map<String,String> mpFun = getRoleFun(selids[j]);
			if (mpRoleFun.isEmpty()) continue;
			String funId = mpFun.get("fun_id");
			
			for (int i = 0, n = lsUser.size(); i < n; i++) {
				Map<String,String> mpUser = lsUser.get(i);
				String userId = mpUser.get("user_id");
				
				_log.showDebug("-------清除数据权限解析SQL缓存，功能ID=" + funId + ", 用户ID=" + userId);
				//清除数据权限解析SQL缓存
				manager.clearDataWhere(userId, funId);
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 取角色功能信息
	 * @param keyId -- 主键值
	 * @return
	 */
	private Map<String,String> getRoleFun(String keyId) {
		String sql = "select role_id, fun_id from sys_role_fun where role_fun_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(keyId);
		
		return _dao.queryMap(param);
	}
}
