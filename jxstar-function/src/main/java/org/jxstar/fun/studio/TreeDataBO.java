/*
 * TreeDataBO.java 2011-3-1
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.studio;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.FileUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 创建树形定义JSON数据文件。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-1
 */
public class TreeDataBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 构建JSON对象。
	 * @return
	 */
	public String createJson(String realPath) {
		List<Map<String,String>> lsTree = queryTreeDefine();
		
		//当前功能ID，一个功能可以有多个树定义
		String curFunId = "";
		//一个树形定义的数据
		StringBuilder sbItem = new StringBuilder();
		//所有树形定义的数据
		StringBuilder sbJson = new StringBuilder("TreeData = {\r");
		for (int i = 0, n = lsTree.size(); i < n; i++) {
			Map<String,String> mpTree = lsTree.get(i);
			
			String funId = mpTree.get("fun_id");
			if (funId.equals(curFunId)) {
			//同一个功能
				sbItem.append(jsonTree(mpTree));
			} else {
			//不是同一个功能
				if (i == 0) {
					sbItem.append("'" + funId + "':[\r");
					sbItem.append(jsonTree(mpTree));
				} else {
					//结束上一个功能
					String oneItem = sbItem.substring(0, sbItem.length()-2);
					oneItem += "\r],\r";
					sbJson.append(oneItem);
					sbItem = sbItem.delete(0, sbItem.length());//清除上一个功能的数据
					
					//开始下一个功能
					curFunId = funId;
					sbItem.append("'" + funId + "':[\r");
					sbItem.append(jsonTree(mpTree));
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
		String fileName = realPath + "/public/data/TreeData.js";
		if (!FileUtil.saveFileUtf8(fileName, sbJson.toString())) {
			setMessage(JsMessage.getValue("Treedata.fcerror"));
			return _returnFaild;
		}
		
		return _returnSuccess;
	}
	
	/**
	 * 取树型定义信息生成前台脚本，需要取设计库中的定义信息。
	 * @return List
	 */
	private List<Map<String,String>> queryTreeDefine() {
		StringBuilder sbsql = new StringBuilder();
		sbsql.append("select fun_id, table_name, node_id, node_name, node_level, ");
		sbsql.append("self_funid, right_where, prop_prefix, has_level, tree_title from fun_tree ");
		
		DaoParam param = _dao.createParam(sbsql.toString());
		param.setDsName(DefineName.DESIGN_NAME);
		return _dao.query(param);
	}
	
	/**
	 * 一个树形定义JSON
	 * @param mpTree -- 树形定义数据
	 * @return
	 */
	private String jsonTree(Map<String,String> mpTree) {
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("\t{");
		
		Iterator<String> itr = mpTree.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			String value = mpTree.get(key);
			//处理字段值中'符号
			value = value.replaceAll("'", "\\\\'");
			
			sbItem.append(key + ":'" + value + "',");
		}
		String item = sbItem.substring(0, sbItem.length()-1);

		return item + "},\r";
	}
}
