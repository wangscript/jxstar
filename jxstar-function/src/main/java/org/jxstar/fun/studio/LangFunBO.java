/*
 * LangFunBO.java 2011-3-31
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
 * 处理模块名、功能名的语言文件。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-31
 */
public class LangFunBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 构建模块名、功能名文件
	 * @param realPath -- 系统路径
	 * @return
	 */
	public String createJson(String realPath) {
		//描述文件
		StringBuilder sbJson = new StringBuilder("jfLang = {\r");
		
		//添加模块名称信息
		sbJson.append(getJson("module", getModuleName()));
		
		//添加功能名称信息
		sbJson.append(getJson("fun", getFunName()));
		
		String json = sbJson.substring(0, sbJson.length()-2) + "\r};";
		
		//_log.showDebug("-------json=" + json);
		
		//生成文件
		String fileName = realPath + "/public/locale/fun-lang-zh.js";
		if (!FileUtil.saveFileUtf8(fileName, json)) {
			setMessage(JsMessage.getValue("combodata.fcerror"));
			return _returnFaild;
		}
		_log.showDebug("-------create success! " + fileName);
		
		return _returnSuccess;
	}
	
	/**
	 * 取一个功能的事件信息
	 * @param funId -- 功能ID
	 * @param lsData -- 名称信息
	 * @return
	 */
	private String getJson(String funId, List<String[]> lsData) {
		StringBuilder sbItem = new StringBuilder();
		sbItem.append("\t'").append(funId).append("':{\r");
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			String[] names = lsData.get(i);
			
			sbItem.append("\t\t'").append(names[0]).append("':'").append(names[1]).append("',\r");
		}
		
		String item = sbItem.substring(0, sbItem.length()-2);
		
		return item + "\r\t},\r";
	}
	
	/**
	 * 取所有模块的名称
	 * @return
	 */
	private List<String[]> getModuleName() {
		List<String[]> lsModName = FactoryUtil.newList();
		
		String sql = "select module_id, module_name from funall_module order by module_id";
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsData = _dao.query(param);
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String code = mpData.get("module_id");
			String name = mpData.get("module_name");
			
			lsModName.add(new String[]{code, name});
		}
		
		return lsModName;
	}
	
	/**
	 * 取所有功能的名称
	 * @return
	 */
	private List<String[]> getFunName() {
		List<String[]> lsFunName = FactoryUtil.newList();
		
		String sql = "select fun_id, fun_name from fun_base order by fun_id";
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lsData = _dao.query(param);
		
		for (int i = 0, n = lsData.size(); i < n; i++) {
			Map<String,String> mpData = lsData.get(i);
			
			String code = mpData.get("fun_id");
			String name = mpData.get("fun_name");
			
			lsFunName.add(new String[]{code, name});
		}
		
		return lsFunName;
	}
}
