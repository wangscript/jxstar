/*
 * ExtPageParser.java 2009-10-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.util.FileUtil;

/**
 * 扩展文件生成类。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-28
 */
public class ExtPageParser extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 取不同页面类型的扩展文件。
	 * @param funcId -- 功能ID
	 * @param pageType -- 页面类型
	 * @param realPath -- 系统路径
	 * @return
	 */
	public String parse(String funcId, String pageType, String realPath) {
		String extFile = getInitPage(funcId, pageType);
		if (extFile.length() == 0) {
			_log.showDebug("提示：没有定义扩展文件！");
			return "";
		}
		String fileName = realPath + extFile;
		_log.showDebug("提示：扩展文件名" + fileName);
		
		return FileUtil.readFileUtf8(fileName);
	}
	
	/**
	 * 取扩展文件的名称或路径
	 * @param funcId
	 * @param pageType
	 * @return
	 */
	public String getInitPage(String funcId, String pageType) {
		String sql = "select grid_initpage, form_initpage from fun_ext where fun_id = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(funcId);
		param.setDsName(DefineName.DESIGN_NAME);
		
		Map<String,String> mpExt = _dao.queryMap(param);
		if (mpExt.isEmpty()) return "";
		
		String file = "";
		if (pageType.indexOf("grid") >= 0) {
			file = mpExt.get("grid_initpage");
		} else {
			file = mpExt.get("form_initpage");
		}
		
		file = file.trim();
		return file;
	}
}
