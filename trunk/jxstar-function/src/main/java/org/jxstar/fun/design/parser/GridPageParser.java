/*
 * PageParser.java 2009-9-27
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.util.List;
import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;


/**
 * GRID页面解析类
 *
 * @author TonyTan
 * @version 1.0, 2009-9-27
 */
public class GridPageParser extends PageParser {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 解析控件的值
	 * @param name
	 * @return
	 */
	protected String elementValue(String name) {
		String ret = "";
		
		if (name.equals("columnModel")) {
			GridParserUtil colParser = new GridParserUtil();

			//取设计信息
			List<Map<String,String>> designData = parseDesignData(_designPage);
			
			String tableName = _funDefine.get("table_name");
			ret = colParser.parse(_funid, tableName, designData);
		} else if (name.equals("incPage")) {
			ExtPageParser extParser = new ExtPageParser();

			ret = extParser.parse(_funid, "grid", _realPath);
			//替换文件中的\转义符
			ret = ret.replace("\\", "\\\\");
			
			if (ret.length() > 0) {//去掉换行符，不然前台取不到文件
				ret = ret.trim();
			}
		} else {
			ret = _elementTpl.get(name);
		}
		
		return ret;
	}
	
	/**
	 * 解析参数的值
	 * @param name
	 * @return
	 */
	protected String paramValue(String name) {
		return _funDefine.get(name);
	}
	
	/**
	 * 解析设计数据，格式：{n:colname,w:width,h:hidden}-{}-...
	 * @return
	 */
	private List<Map<String,String>> parseDesignData(String data) {
		List<Map<String,String>> lsData = FactoryUtil.newList();
		
		if (data == null || data.length() == 0) {
			return lsData;
		}
		
		String[] datas = data.split("-");
		for (int i = 0, n = datas.length; i < n; i++) {
			String param = datas[i].substring(1, datas[i].length()-1);
			String[] params = param.split(",");
			
			Map<String,String> mp = FactoryUtil.newMap();
			for (int j = 0; j < params.length; j++) {
				String[] ps = params[j].split(":");
				if (ps.length < 2) continue;
				mp.put(ps[0], ps[1]);
			}
			
			lsData.add(mp);
		}
		//_log.showDebug("design data=" + lsData.toString());
		return lsData;
	}
}
