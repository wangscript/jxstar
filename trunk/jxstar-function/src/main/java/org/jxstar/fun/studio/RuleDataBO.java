/*
 * RuleData.java 2009-12-26
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.FileUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 构建规则定义中路由条件的JSON对象。
 *
 * @author TonyTan
 * @version 1.0, 2009-12-26
 */
public class RuleDataBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 构建规则定义中路由条件的JSON对象
	 * @return
	 */
	public String createJson(String realPath) {
		String sql = "select fun_id, src_funid, where_sql, where_type, where_value, layout_page "+
					 "from fun_rule_route order by fun_id";
		
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsRule = _dao.query(param);
		
		//当前功能ID，一个功能可以有多个数据来源
		String curFunId = "";
		//一个路由条件的数据
		StringBuilder sbItem = new StringBuilder();
		//所有路由条件的数据
		StringBuilder sbJson = new StringBuilder("RuleData = {\r");
		for (int i = 0, n = lsRule.size(); i < n; i++) {
			Map<String,String> mpRule = lsRule.get(i);

			String funId = mpRule.get("fun_id");
			if (funId.equals(curFunId)) {
			//同一个功能
				sbItem.append(jsonRoute(mpRule));
			} else {
			//不是同一个功能
				if (i == 0) {
					sbItem.append("'" + funId + "':[\r");
					sbItem.append(jsonRoute(mpRule));
				} else {
					//结束上一个功能
					String oneItem = sbItem.substring(0, sbItem.length()-2);
					oneItem += "\r],\r";
					sbJson.append(oneItem);
					sbItem = sbItem.delete(0, sbItem.length());//清除上一个功能的数据
					
					//开始下一个功能
					curFunId = funId;
					sbItem.append("'" + funId + "':[\r");
					sbItem.append(jsonRoute(mpRule));
				}
			}
		}
		if (sbItem.length() > 0) {
			String item = sbItem.substring(0, sbItem.length()-2);
			sbJson.append(item).append("\r]\r};");
		} else {
			sbJson.append("};");
		}
		
		//数据生成文件
		String fileName = realPath + "/public/data/RuleData.js";
		if (!FileUtil.saveFileUtf8(fileName, sbJson.toString())) {
			setMessage(JsMessage.getValue("ruledata.fcerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	//一个路由条件
	private String jsonRoute(Map<String,String> mpRule) {
		String whereSql = mpRule.get("where_sql");
		whereSql = whereSql.replace("'", "\\'");
		
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("\t{srcNodeId:'" + mpRule.get("src_funid") + "',");
		sbItem.append("destNodeId:'" + mpRule.get("fun_id") + "',");
		sbItem.append("layout:'" + mpRule.get("layout_page") + "',");
		sbItem.append("whereSql:'" + whereSql + "',");
		sbItem.append("whereType:'" + mpRule.get("where_type") + "',");
		sbItem.append("whereValue:'" + mpRule.get("where_value") + "'},\r");
		
		return sbItem.toString();
	}
}
