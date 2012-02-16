/*
 * LangTextBO.java 2011-3-31
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
 * 自定义语言文字处理类，约定文字键值只有两级，如：xxxx.yyyy，
 * 这样方便构建文字JSON对象与读取文字描述。
 *
 * @author TonyTan
 * @version 1.0, 2011-3-31
 */
public class LangTextBO extends BusinessObject {
	private static final long serialVersionUID = 1L;

	/**
	 * 生成自定义语言文字文件
	 * @param realPath
	 * @return
	 */
	public String createJson(String realPath) {
		String sql = "select prop_key, prop_value from funall_text order by prop_key";
		
		DaoParam param = _dao.createParam(sql);
		param.setDsName(DefineName.DESIGN_NAME);
		List<Map<String,String>> lstext = _dao.query(param);
		
		//键值前缀
		String prefix = "";
		//一个文字描述值
		StringBuilder sbItem = new StringBuilder();
		//描述文件
		StringBuilder sbJson = new StringBuilder("jx = {\r");
		for (int i = 0, n = lstext.size(); i < n; i++) {
			Map<String,String> mptext = lstext.get(i);
			
			String key = mptext.get("prop_key");
			String value = mptext.get("prop_value");
			
			if (key.length() == 0) continue;
			
			String[] keys = getKeys(key);
			//开始新的文字组
			if (!keys[0].equals(prefix)) {
				//如果不是第一个文字
				if (i > 0) {
					String item = sbItem.substring(0, sbItem.length()-2);
					sbJson.append(item).append("\r\t},\r");
					
					//清除原文字组的数据
					sbItem = sbItem.delete(0, sbItem.length());
				}
				
				sbItem.append("\t'").append(keys[0]).append("':{\r");
				//文字组
				prefix = keys[0];
			}
			
			sbItem.append("\t\t'").append(keys[1]).append("':'").append(value).append("',\r");
		}
		//最后一个文字组
		if (sbItem.length() > 0) {
			String item = sbItem.substring(0, sbItem.length()-2);
			sbJson.append(item).append("\r\t}\r};");
		} else {
			sbJson.append("};");
		}
		
		//_log.showDebug("-------json=" + sbJson.toString());
		
		//生成文件
		String fileName = realPath + "/public/locale/jxstar-lang-zh.js";
		String content = sbJson.toString();
		if (!FileUtil.saveFileUtf8(fileName, content)) {
			setMessage(JsMessage.getValue("combodata.fcerror"));
			return _returnFaild;
		}
		_log.showDebug("-------create success! " + fileName);
		
		return _returnSuccess;
	}
	
	//取一级键值、二级键值
	private String[] getKeys(String key) {
		String[] keys;
		
		int index = key.indexOf(".");
		if (index > 0){
			keys = new String[]{key.substring(0, index), key.substring(index + 1)};
		} else {
			keys = new String[]{key, "notwokey"};
		}
		
		return keys;
	}
}
