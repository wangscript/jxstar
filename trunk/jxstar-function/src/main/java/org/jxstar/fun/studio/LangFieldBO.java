/*
 * LangFieldBO.java 2011-3-31
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
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * 生成功能字段的语言文件，可以一次生成所有功能的字段语言文件，
 * 也可以生成单个功能的字段语言文件。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-31
 */
public class LangFieldBO extends BusinessObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 构建所有功能的字段文字
	 * @param realPath -- 系统路径
	 * @return
	 */
	public String createJson(String realPath) {
		String sql = "select fun_id, fun_name from fun_base order by fun_id";
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsFun = _dao.query(param);
		
		for (int i = 0, n = lsFun.size(); i < n; i++) {
			Map<String,String> mpFun = lsFun.get(i);
			
			String funId = mpFun.get("fun_id");
			createJson(realPath, funId);
		}
		
		return _returnSuccess;
	}

	/**
	 * 构建一个功能的字段文字
	 * @param realPath -- 系统路径
	 * @param funId -- 功能ID
	 * @return
	 */
	public String createJson(String realPath, String funId) {
		String json = getFunJson(funId);
		//_log.showDebug("-------json=" + json);
		
		//生成文件
		String fileName = realPath + "/public/locale/field/"+ funId +"-lang-zh.js";
		if (!FileUtil.saveFileUtf8(fileName, json)) {
			setMessage(JsMessage.getValue("combodata.fcerror"));
			return _returnFaild;
		}
		_log.showDebug("-------create success! " + fileName);
		
		return _returnSuccess;
	}
	
	/**
	 * 取一个功能的字段文字描述
	 * @param funId -- 功能ID
	 * @return
	 */
	private String getFunJson(String funId) {
		List<String[]> lsField = getFieldName(funId);
		
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("jdLang['").append(funId).append("'] = {\r");
		
		for (int i = 0, n = lsField.size(); i < n; i++) {
			String[] names = lsField.get(i);
			
			sbItem.append("\t'").append(names[0]).append("':'").append(names[1]).append("',\r");
		}
		String item = sbItem.substring(0, sbItem.length()-2);
		
		return item + "\r};";
	}
	
	/**
	 * 取一个功能的字段名称
	 * @param funId -- 功能ID
	 * @return
	 */
	private List<String[]> getFieldName(String funId) {
		List<String[]> lsField = FactoryUtil.newList();
		
		String sql = "select col_code, col_name from fun_col where fun_id = ? order by col_index";
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funId);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsData = _dao.query(param);
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String code = mpData.get("col_code");
			String name = mpData.get("col_name");
			
			code = code.replace(".", "__");
			lsField.add(new String[]{code, name});
		}
		
		return lsField;
	}
}
