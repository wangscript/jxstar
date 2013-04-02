/*
 * SysUserBO.java 2010-11-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.studio;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.security.Password;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.util.SysDataManager;
import org.jxstar.util.MapUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 处理用户信息的类。
 *
 * @author TonyTan
 * @version 1.0, 2010-11-23
 */
public class SysUserBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 修改用户数据权限后需要清除缓存
	 * @param userId -- 选择的用户ID
	 * @return
	 */
	public String clearDataWhere(String[] userIds) {
		_log.showDebug("-------清除数据权限解析SQL缓存，用户ID=" + userIds.length);
		
		SysDataManager manager = SysDataManager.getInstance();
		for (int i = 0, n = userIds.length; i < n; i++) {
			manager.clearDataWhere(userIds[i]);
		}
		
		return _returnSuccess;
	}

	/**
	 * 修改用户密码
	 * @param userId -- 用户ID
	 * @param oldPass -- 原密码
	 * @param newPass -- 新密码
	 * @return
	 */
	public String setPass(String userId, String oldPass, String newPass) {
		if (userId == null || userId.length() == 0 || 
				oldPass == null || oldPass.length() == 0 || 
				newPass == null || newPass.length() == 0) {
			//"修改密码的参数不正确！"
			setMessage(JsMessage.getValue("sysuserbo.paramerror"));
			return _returnFaild;
		}
		_log.showDebug("userid=" +userId + " oldpass=" + oldPass + " newpass=" + newPass);
		
		if (!checkOldPass(userId, oldPass)) {
			//"密码修改失败，旧密码无效！"
			setMessage(JsMessage.getValue("sysuserbo.modifyerror"));
			return _returnFaild;
		}
		
		if (!updatePass(userId, newPass)) {
			//"更新新密码失败！"
			setMessage(JsMessage.getValue("sysuserbo.updateerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 修改为缺省密码
	 * @param userId
	 * @return
	 */
	public String defaultPass(String[] userIds) {
		for (int i = 0, n = userIds.length; i < n; i++) {
			if (!updatePass(userIds[i], "888")) {
				setMessage(JsMessage.getValue("sysuserbo.updateerror"));
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 新增用户时创建新的缺省密码
	 * @param userIds
	 * @return
	 */
	public String createPass(String[] userIds) {
		for (int i = 0, n = userIds.length; i < n; i++) {
			//如果有密码了，则不修改
			if (hasPass(userIds[i])) continue;
			
			if (!updatePass(userIds[i], "888")) {
				setMessage(JsMessage.getValue("sysuserbo.updateerror"));
				return _returnFaild;
			}
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 修改密码
	 * @param userId -- 用户ID
	 * @param newPass -- 新密码
	 * @return
	 */
	private boolean updatePass(String userId, String newPass) {
		String newpass = Password.md5(newPass);
		String sql = "update sys_user set user_pwd = ? where user_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(newpass);
		param.addStringValue(userId);

		return _dao.update(param);
	}
	
	/**
	 * 检查旧密码是否有效
	 * @param userId -- 用户ID
	 * @param oldPass -- 旧密码
	 * @return
	 */
	private boolean checkOldPass(String userId, String oldPass) {
		String oldpass = Password.md5(oldPass);
		String sql = "select count(*) as cnt from sys_user where user_pwd = ? and user_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(oldpass);
		param.addStringValue(userId);
		
		Map<String,String> mp = _dao.queryMap(param);
		return MapUtil.hasRecord(mp);
	}
	
	/**
	 * 判断用户是否有密码
	 * @param userId
	 * @return
	 */
	private boolean hasPass(String userId) {
		String sql = "select user_pwd from sys_user where user_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(userId);
		
		Map<String,String> mp = _dao.queryMap(param);
		if (mp.isEmpty()) return false;
		
		return mp.get("user_pwd").length() > 0;
	}
}
