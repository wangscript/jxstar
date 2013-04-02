/*
 * MenuQuery.java 2010-3-7
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.util.SysUserUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 创建功能模块。
 *
 * @author TonyTan
 * @version 1.0, 2010-3-7
 */
public class MenuQuery extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 返回用户菜单
	 * @param userId -- 用户ID
	 * @return 
	 */
	public String createMainMenu(String userId) {
		if (userId == null || userId.length() == 0) {
			//"提示：当前用户ID为空！"
			setMessage(JsMessage.getValue("menuquery.useridnull"));
			return _returnFaild;
		}
		//取一级模块的过滤语句
		String oneWhere = "";
		//保存每个模块有哪些功能信息
		Map<String,List<Map<String,String>>> moduleFun = FactoryUtil.newMap();
		//保存二级模块ID信息
		Map<String,StringBuilder> towModule = FactoryUtil.newMap();
		
		//判断用户是否为系统管理员
		boolean isAdmin = SysUserUtil.isAdmin(userId);
		_log.showDebug("-----------current user role is admin=" + isAdmin);
				
		if (!isAdmin) {
			//取当前用户所有有权限的功能信息
			List<Map<String,String>> lsFun = queryAllFun(userId);
			if (lsFun.isEmpty()) {
				//"提示：当前用户【{0}】没有一个功能的使用权限！"
				setMessage(JsMessage.getValue("menuquery.nofunid"), userId);
				return _returnFaild;
			}
			
			//构建一级模块ID串
			StringBuilder sbOne = new StringBuilder();
			
			//把功能信息与模块信息分离出来
			String oldTwoId = "", oldOneId = "";
			for (int i = 0, n = lsFun.size(); i < n; i++) {
				Map<String,String> mpFun = lsFun.get(i);
				
				//二级模块ID
				String twoId = mpFun.get("module_id");
				//一级模块ID
				String oneId = twoId.substring(0, 4);
				
				//一级模块ID不同时构建新对象保存
				if (!oldOneId.equals(oneId)) {
					oldOneId = oneId;
					
					//构建一级模块ID字符串
					sbOne.append("'").append(oneId).append("',");
					
					//构建二级模块ID串
					StringBuilder sbTwo = new StringBuilder();
					towModule.put(oneId, sbTwo);
				}
				
				//二级模块ID不同时构建新对象保存
				if (!oldTwoId.equals(twoId)) {
					oldTwoId = twoId;
					
					//保存二级模块ID信息
					towModule.get(oneId).append("'").append(twoId).append("',");
					
					List<Map<String,String>> tmpFun = FactoryUtil.newList();
					moduleFun.put(twoId, tmpFun);
				}
				
				//保存各二级模块的功能信息
				moduleFun.get(twoId).add(mpFun);
			}
			//--------------------------------------------------------------------------
			
			//取一级模块的过滤语句
			oneWhere = sbOne.substring(0, sbOne.length()-1);
			oneWhere = "module_id in (" + oneWhere + ")";
		}
		
		//一级模块信息
		_log.showDebug("-----------one module id=" + oneWhere);
		List<Map<String,String>> lsModule = queryModule(oneWhere);
		if (lsModule.isEmpty()) {
			//"提示：当前用户【{0}】没有查询到一级模块信息！"
			setMessage(JsMessage.getValue("menuquery.nomodule"), userId);
			return _returnFaild;
		}
		
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsModule.size(); i < n; i++) {
			Map<String,String> mpModule = lsModule.get(i);
			
			//一级模块ID
			String moduleId = mpModule.get("module_id");
			//一级模块名称
			String moduleName = mpModule.get("module_name");
			//一级模块是否展开
			String isExpanded = mpModule.get("is_expanded");
			isExpanded = isExpanded.equals("1") ? "true" : "false";
			
			//取二级模块的过滤语句
			String twoWhere = null;
			if (!isAdmin) {
				StringBuilder sbTwo = towModule.get(moduleId);
				twoWhere = sbTwo.substring(0, sbTwo.length()-1);
				twoWhere = "module_id in (" + twoWhere + ")";
			} else {
				twoWhere = "module_id like '"+ moduleId +"%'";
			}
			//取二级模块值
			_log.showDebug("-----------two module id=" + twoWhere);
			List<Map<String,String>> lsTwoModule= queryTwoModule(twoWhere);
			if (lsTwoModule.isEmpty()) continue;
			
			//构建一级模块
			sbJson.append("{id:'"+ moduleId +"', ");
			sbJson.append("text:'"+ moduleName +"', ");
			sbJson.append("leaf:false, cls:'one-menu', expanded:"+ isExpanded +", ");
			sbJson.append("children: ");
			
			//构建二级模块
			sbJson.append(createTwoMenu(lsTwoModule, moduleFun));
			
			//一级模块结束
			sbJson.append("},");
		}
		String json = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		//_log.showDebug("===============json=" + json);
		setReturnData(json);
		
		return _returnSuccess;
	}
		
	/**
	 * 构建二级模块的菜单
	 * @param lsModule -- 二级模块信息
	 * @param moduleFun -- 二级模块ID与对应的功能信息
	 * @return 返回JSON格式：[{module_id:'', text:'', menu:new Ext.menu.Menu}, {}...]
	 */
	private String createTwoMenu(List<Map<String,String>> lsModule,
								 Map<String,List<Map<String,String>>> moduleFun) {
		String retJson = "[]";
		if (lsModule == null || lsModule.isEmpty()) return retJson;
		
		//取平台版本类型，如果是标准版，则不显示工作流与报表模块
		//String verType = SystemVar.getValue("sys.version.type", "SE");
		
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsModule.size(); i < n; i++) {
			Map<String,String> mpModule = lsModule.get(i);
			
			//二级模块ID
			String moduleId = mpModule.get("module_id");
			/*
			if (verType.equals("SE")) {
				if (moduleId.equals("10100003") || moduleId.equals("10100004")) {
					continue;
				}
			}*/
			
			//二级模块名称
			String moduleName = mpModule.get("module_name");
			//二级模块是否展开
			String isExpanded = mpModule.get("is_expanded");
			isExpanded = isExpanded.equals("1") ? "true" : "false";
			
			//查询功能菜单
			List<Map<String,String>> lsFun = null;
			//如果moduleFun为空，说明是系统管理员
			if (moduleFun.isEmpty()) {
				lsFun = queryFun(moduleId);
			} else {
				lsFun = moduleFun.get(moduleId);
			}
			
			//构建二级模块
			sbJson.append("{id:'"+ moduleId +"', ");
			sbJson.append("text:'"+ moduleName +"', ");
			sbJson.append("leaf:false, cls:'two-menu', expanded:"+ isExpanded +", ");
			sbJson.append("children: ");
			
			//构建功能菜单
			if (lsFun.isEmpty()) {
				sbJson.append("[]");
			} else {
				StringBuilder sbFun = new StringBuilder();
				for (int j = 0, m = lsFun.size(); j < m; j++) {
					Map<String,String> mpFun = lsFun.get(j);
					
					sbFun.append("{id:'"+ mpFun.get("fun_id") +"', ");
					sbFun.append("text:'"+ mpFun.get("fun_name") +"',");
					sbFun.append("leaf:true, cls:'three-menu' },");
				}
				
				String sfun = "[" + sbFun.substring(0, sbFun.length()-1) + "]";
				sbJson.append(sfun);
			}
			
			//二级模块结束
			sbJson.append("},");
		}
		if (sbJson.length() == 0) return retJson;
		retJson = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		
		return retJson;
	}
	
	/**
	 * 创建该用户的功能模块
	 * @param userId -- 用户ID
	 * @return 返回JSON格式：[{module_id:'', module_name:''}, {}...]
	 */
/*	public String createModule(String userId) {
		List<Map<String,String>> lsModule = queryModule(userId);
		if (lsModule.isEmpty()) {
			_log.showWarn("提示：没有模块信息！");
			return _returnFaild;
		}
		
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsModule.size(); i < n; i++) {
			Map<String,String> mpModule = lsModule.get(i);
			
			sbJson.append("{module_id:'"+mpModule.get("module_id"));
			sbJson.append("', module_name:'"+mpModule.get("module_name")+"'},");
		}
		String json = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		_log.showDebug("===============json=" + json);
		setReturnData(json);
		
		return _returnSuccess;
	}*/
	
	/**
	 * 创建功能菜单
	 * @param userId -- 用户ID
	 * @param moduleId -- 一级模块ID
	 * @return 
	 */
/*	public String createTree(String userId, String moduleId) {
		List<Map<String,String>> lsModule = queryTwoModule(userId, moduleId);
		if (lsModule.isEmpty()) {
			_log.showWarn("提示：没有二级模块信息！");
			return _returnFaild;
		}
		
		StringBuilder sbJson = new StringBuilder();
		for (int i = 0, n = lsModule.size(); i < n; i++) {
			Map<String,String> mpModule = lsModule.get(i);
			
			//二级模块信息
			String towId = mpModule.get("module_id");
			sbJson.append("{id:'"+towId+"', ");
			sbJson.append("text:'"+mpModule.get("module_name")+"', ");
			sbJson.append("expanded: true, ");
			
			//功能信息
			List<Map<String,String>> lsFun = queryFun(userId, towId);
			if (!lsFun.isEmpty()) {
				StringBuilder sbFun = new StringBuilder();
				for (int j = 0, m = lsFun.size(); j < m; j++) {
					Map<String,String> mpFun = lsFun.get(j);
					
					sbFun.append("{id:'"+mpFun.get("fun_id")+"', ");
					sbFun.append("text:'"+mpFun.get("fun_name")+"', ");
					sbFun.append("iconCls:'nav_fun', leaf:true},");
				}
				String sfun = "[" + sbFun.substring(0, sbFun.length()-1) + "]";
				sbJson.append("children:" + sfun);
			} else {
				sbJson.append("iconCls:'nav_fun', leaf:true");
			}
			
			//二级模块结束
			sbJson.append("},");
		}
		String json = "[" + sbJson.substring(0, sbJson.length()-1) + "]";
		_log.showDebug("===============json=" + json);
		setReturnData(json);
		
		return _returnSuccess;
	}*/
	
	/**
	 * 取当前用户有权限的所有功能信息
	 * @param userId
	 * @return
	 */
	private List<Map<String,String>> queryAllFun(String userId) {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select fun_id, fun_name, module_id from fun_base ");
		sbsql.append("where reg_type in ('main', 'treemain') and fun_id in ");
		sbsql.append("(select distinct sys_role_fun.fun_id from sys_user_role, sys_role_fun ");
		sbsql.append("where sys_user_role.role_id = sys_role_fun.role_id ");
		sbsql.append("and sys_user_role.user_id = ? ) ");
		sbsql.append("order by module_id, fun_index");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(userId);
		return _dao.query(param);
	}
	
	/**
	 * 取二级模块对应的功能ID
	 * @param moduleId -- 二级模块ID
	 * @return List
	 */
	private List<Map<String,String>> queryFun(String moduleId) {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select fun_id, fun_name from fun_base where module_id = ? and reg_type in ('main', 'treemain') order by fun_index");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.addStringValue(moduleId);
		return _dao.query(param);
	}
	
	/**
	 * 取二级模块信息
	 * @param twoWhere -- 二级模块ID过滤语句
	 * @return List
	 */
	private List<Map<String,String>> queryTwoModule(String twoWhere) {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select module_id, module_name, is_expanded from funall_module ");
		sbsql.append("where module_level = 2 and is_show = '1' ");
		if (twoWhere != null && twoWhere.length() > 0) {
			sbsql.append(" and " + twoWhere);
		}
		sbsql.append(" order by module_index");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		return _dao.query(param);
	}
	
	/**
	 * 取一级模块信息
	 * @param oneWhere -- 一级模块ID过滤语句
	 * @return List
	 */
	private List<Map<String,String>> queryModule(String oneWhere) {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select module_id, module_name, is_expanded from funall_module ");
		sbsql.append("where module_level = 1 and is_show = '1' ");
		if (oneWhere != null && oneWhere.length() > 0) {
			sbsql.append(" and " + oneWhere);
		}
		sbsql.append(" order by module_index");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		return _dao.query(param);
	}
}
