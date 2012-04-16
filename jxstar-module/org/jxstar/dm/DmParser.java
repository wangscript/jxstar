/*
 * DmParser.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jxstar.dm.util.DmTemplet;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 数据库配置信息解析类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public abstract class DmParser {
	//日志对象
	private Log _log = Log.getInstance();
	//参数表达式
	private static String _param_regex = "<param:[^/>]+/>";
	//元素表达式
	private static String _element_regex = "<element:[^/>]+/>";
	//当前数据库类型
	private String _dbtype = null;
	//当前数据库的模板定义
	private Map<String,String> _templet = null;
	
	protected DmParser(String dbType) {
		_dbtype = dbType;
		//当前数据源类型的模板文件
		_templet = getTemplet(dbType);
	}
	
	/**
	 * 解析模板文件中的元素
	 * @param name -- 元素名
	 * @param mpData -- 解析用的数据
	 * @return
	 */
	public String parseTemplet(String name, Map<String,String> mpData) throws DmException {
		if (_templet == null || _templet.isEmpty()) {
			//"数据库类型【{0}】的模板文件没有加载！"
			throw new DmException(JsMessage.getValue("dmparser.notemplet"), _dbtype);
		}
		
		//取模板文件
		String templet = _templet.get(name);
		
		//有些数据库的元素会配置空元素
		if (templet == null || templet.trim().length() == 0) {
			_log.showDebug("templet is null, element name is:" + name);
			return "";
		}
		
		//去掉解析模板两头的空格
		templet = templet.trim();
		
		//解析脚本中的变量
		templet = parseTemplet(templet, "element", mpData);
		
		//解析脚本中的字段
		templet = parseTemplet(templet, "param", mpData);
		
		//添加换行符
		templet += "\r\n";
		
		return templet;
	}
	
	/**
	 * 解析变量的值
	 * @param name -- 变量名
	 * @param mpData -- 解析用的数据
	 * @return
	 */
	protected abstract String parseElement(String name, Map<String,String> mpData) throws DmException;	
	
	/**
	 * 解析模板文件中的元素
	 * @param templet -- 模板文件
	 * @param type -- 元素类型[element|param]
	 * @return
	 */
	private String parseTemplet(String templet, String type, Map<String,String> mpData) throws DmException {
		String regex = _param_regex;
		if (type.equals("element")) {
			regex = _element_regex;
		}
		
		//创建匹配对象
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(templet);
		
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			//取元素名称
			String tag = m.group();
			String name = tagName(tag);
			
			//取元素的值
			String reptext = "";
			if (type.equals("element")) {
				reptext = parseElement(name, mpData);
			} else {
				reptext = mpData.get(name);
			}
			if (reptext == null) {
				//"模板中的参数【{0}】没有解析成功！"
				throw new DmException(JsMessage.getValue("dmparser.parseerror"), name);
			}
			
			//替换元素的值
			m.appendReplacement(sb, reptext);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 解析标签中的名字，如<element:page/>取page的值
	 * @param tag
	 * @return
	 */
	private String tagName(String tag) {
		return tag.substring(tag.indexOf(":")+1, tag.length()-2);
	}
	
	/**
	 * 取SQL模板定义
	 * @param dbType -- 数据库类型
	 * @return
	 */
	private Map<String,String> getTemplet(String dbType) {
		return DmTemplet.getInstance().getElementMap(dbType);
	}
}
