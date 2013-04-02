/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.control.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.MapUtil;
import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 单点登录的工具类。
 *
 * @author TonyTan
 * @version 1.0, 2012-12-29
 */
public class OneLoginUtil {
	private static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 取会话信息中的用户信息，并转换为JSON数据。
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getSessionData(HttpServletRequest request) {
		Map<String,String> mpUser = (Map<String,String>) request.getSession().getAttribute("curruser");
		if (mpUser == null || mpUser.isEmpty()) return "";
		
		return MapUtil.toJson(mpUser);
	}
	
	/**
	 * 给用户信息添加组织信息
	 * @param mpUser
	 * @return
	 */
	public static Map<String,String> addOrgData(Map<String,String> mpUser) {
		Map<String,String> retData = FactoryUtil.newMap();
		//把原值都复制过来
		retData.putAll(mpUser);
		
		String deptId = MapUtil.getValue(mpUser, "dept_id");
		if (deptId.length() < 4) return retData;
		
		String rootid = deptId.substring(0, 4);
		//如果是本部单位，则直接取当前部门
		if (rootid.equals("1001")) {
			retData.put("orgid", mpUser.get("dept_id"));
			retData.put("orgcode", mpUser.get("dept_code"));
			retData.put("orgname", mpUser.get("dept_name"));
		} else {
			String sql = "select dept_id, dept_code, dept_name from sys_dept where dept_id = ?";
			DaoParam param = _dao.createParam(sql);
			param.addStringValue(rootid);
			Map<String,String> mpData = _dao.queryMap(param);
			
			retData.put("orgid", MapUtil.getValue(mpData, "dept_id"));
			retData.put("orgcode", MapUtil.getValue(mpData, "dept_code"));
			retData.put("orgname", MapUtil.getValue(mpData, "dept_name"));
		}
		
		return retData;
	}
	
	/**
	 * 根据账号获取用户信息。
	 * @param userCode
	 * @return
	 */
	public static Map<String,String> getUserMap(String userCode) {
		//从数据库中去用户信息
		Map<String,String> mpUser = SysUserUtil.getUserByCode(userCode);
		if (mpUser.isEmpty()) return mpUser;
		
		//取当前用户的角色
		String userId = (String) mpUser.get("user_id");
		String roleId = SysUserUtil.getRoleID(userId);
		if (roleId.length() == 0) return null;
		
		//保存角色ID
		mpUser.put("role_id", roleId);
		mpUser.put("project_path", SystemVar.REALPATH);
		
		return mpUser;
	}
}
