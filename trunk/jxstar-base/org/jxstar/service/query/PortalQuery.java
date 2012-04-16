/*
 * PortalQuery.java 2010-12-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service.query;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BoException;
import org.jxstar.service.BusinessObject;
import org.jxstar.util.resource.JsMessage;

/**
 * 查询首页portal配置信息。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-30
 */
public class PortalQuery extends BusinessObject {
	private static final long serialVersionUID = -9049771487793009958L;

	/**
	 * 取配置信息，构建成JSON对象
	 * @param userId -- 用户信息
	 * @return
	 */
	public String getPortalJson(String userId) {
		//取用户所属角色的模板信息
		List<Map<String,String>> lsTemp = queryTemplet(userId);
		if (lsTemp.isEmpty()) {
			//"构建首页失败，因为用户所属角色没有定义PORTAL模板！"
			setMessage(JsMessage.getValue("portalquery.notemp"));
			return _returnFaild;
		}
		
		//构建PORTAL的JSON
		StringBuilder sbtemps = new StringBuilder();
		for (int i = 0, n = lsTemp.size(); i < n; i++) {
			Map<String,String> mpTemp = lsTemp.get(i);
			
			try {
				String tempJson = templetJson(mpTemp);
				
				sbtemps.append(tempJson);
				sbtemps.append((i < n - 1) ? ",\n" : "\n");
			} catch (BoException e) {
				_log.showError(e);
				setMessage(e.getMessage());
				return _returnFaild;
			}
		}
		
		String portalJson = "{portalnum:"+ lsTemp.size() +", portals:[\n"+ sbtemps.toString() +"]}";
		_log.showDebug("---------portalJson=" + portalJson);
		setReturnData(portalJson);
		
		return _returnSuccess;
	}
	
	/**
	 * 构建一个模板的JSON
	 * @param mpTemplet -- 模板信息
	 * @return
	 */
	private String templetJson(Map<String,String> mpTemplet) throws BoException {
		StringBuilder sbtemp = new StringBuilder();
		
		String templetId = mpTemplet.get("templet_id");
		String templetName = mpTemplet.get("templet_name");
		String col_num = mpTemplet.get("col_num");
		String col_width = mpTemplet.get("col_width");
		
		//取列内容的JSON
		String colitems = colItemsJson(templetId, templetName);
		
		//构建列对象的JSON
		sbtemp.append("\t{templet_name:'"+ templetName +"',");
		sbtemp.append("col_num:"+ col_num +",");
		sbtemp.append("col_width:'"+ col_width +"',");
		sbtemp.append("items:[\n"+ colitems +"\t]}");
		
		return sbtemp.toString();
	}
	
	/**
	 * 取列内容的JSON
	 * @param templetId -- 模板ID
	 * @param templetName -- 模板名称
	 * @return
	 */
	private String colItemsJson(String templetId, String templetName) throws BoException {
		//取模板栏目
		List<Map<String,String>> lsPort = queryPortlet(templetId);
		if (lsPort.isEmpty()) {
			//"构建首页失败，因为PORTAL模板【{0}】的栏目明细为空！"
			throw new BoException(JsMessage.getValue("portalquery.notempdet"), templetName);
		}
		
		StringBuilder sbitems = new StringBuilder();
		for (int i = 0, n = lsPort.size(); i < n; i++) {
			Map<String,String> mpPort = lsPort.get(i);

			sbitems.append("\t\t{id:'"+ mpPort.get("portlet_id") +"', ");
			sbitems.append("title:'"+ mpPort.get("portlet_title") +"', ");
			sbitems.append("colno:'"+ mpPort.get("col_no") +"', ");
			sbitems.append("iconCls:'"+ mpPort.get("iconcls") +"', ");
			sbitems.append("height:'"+ mpPort.get("height") +"', ");
			sbitems.append("collapse:'"+ mpPort.get("collapse") +"', ");
			sbitems.append("typecode:'"+ mpPort.get("type_code") +"', ");
			sbitems.append("objectid:'"+ mpPort.get("object_id") +"'}");
			sbitems.append((i < n - 1) ? ",\n" : "\n");
		}
		
		return sbitems.toString();
	}
	
	/**
	 * 取模板栏目内容的详细信息
	 * @param templetId -- 模板ID
	 * @return
	 */
	private List<Map<String,String>> queryPortlet(String templetId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select plet_type.type_code, plet_type.iconcls, plet_type.height, ");
		sql.append("plet_portlet.portlet_id, plet_portlet.portlet_no, ");
		sql.append("plet_portlet.portlet_title, plet_portlet.collapse,");
		sql.append("plet_portlet.col_no, plet_portlet.templet_id, ");
		sql.append("plet_portlet.object_id, plet_portlet.object_name ");
		sql.append("from plet_portlet, plet_type ");
		sql.append("where plet_portlet.type_code = plet_type.type_code and plet_portlet.templet_id = ? ");
		sql.append("order by plet_portlet.col_no, plet_portlet.portlet_no ");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(templetId);
		return _dao.query(param);
	}
	
	/**
	 * 取用户所属角色的模板信息
	 * @param userId -- 用户ID
	 * @return
	 */
	private List<Map<String,String>> queryTemplet(String userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select templet_no, templet_name, col_num, col_width, templet_id ");
		sql.append("from plet_templet where templet_id in ");
		sql.append("(select sys_role.templet_id from sys_role, sys_user_role where ");
		sql.append("sys_role.role_id = sys_user_role.role_id and sys_user_role.user_id = ?) ");
		sql.append("order by templet_no ");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.addStringValue(userId);
		return _dao.query(param);
	}
}
