/*
 * PageParser.java 2009-10-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.fun.design.parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.jxstar.dao.util.BigFieldUtil;
import org.jxstar.fun.design.templet.ElementTemplet;
import org.jxstar.fun.design.templet.PageTemplet;
import org.jxstar.service.BusinessObject;
import org.jxstar.service.define.DefineName;
import org.jxstar.service.define.FunDefineDao;
import org.jxstar.util.resource.JsMessage;

/**
 * 页面解析基础类，只有页面元素的解析方法不同。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-31
 */
public abstract class PageParser extends BusinessObject {
	private static final long serialVersionUID = 1L;

	//参数表达式
	protected static String _param_regex = "<param:[^/>]+/>";
	//元素表达式
	protected static String _element_regex = "<element:[^/>]+/>";
	//功能ID
	protected String _funid = null;
	//设置系统路径
	protected String _realPath = "";
	//页面设计信息
	protected String _designPage = "";
	//功能定义信息
	protected Map<String,String> _funDefine = null;
	//元素模板文件
	protected Map<String,String> _elementTpl = null;
	
	/**
	 * 设置系统路径
	 * @param realPath
	 */
	public void setRealPath(String realPath) {
		_realPath = realPath;
	}

	/**
	 * 解析模板文件主要方法
	 * @param funcId
	 * @return
	 */
	public String parsePage(String funcId, String pageType) {
		if (funcId == null || funcId.length() == 0) {
			//"错误参数：功能ID不能为！"
			_log.showWarn(JsMessage.getValue("pageparser.idnull"));
			return _returnFaild;
		}
		
		String page = PageTemplet.getInstance().getPage(pageType);
		if (page == null || page.length() == 0) {//"读取GRID模板文件失败！"
			_log.showWarn(JsMessage.getValue("pageparser.notemp"));
			return _returnFaild;
		}
		
		//取页面设计信息
		_designPage = readDesignPage(funcId, pageType);
		
		//取功能定义信息
		_funid = funcId;
		_funDefine = FunDefineDao.queryFun(funcId);
		
		//取页面名称
		String pageName = "";
		if (pageType.indexOf("grid") >= 0) {
			pageName = _funDefine.get("grid_page");
		} else {
			pageName = _funDefine.get("form_page");
		}
		if (pageName.length() == 0) {
			setMessage(JsMessage.getValue("pageparser.nopage"));
			return _returnFaild;
		}
		
		//取元素模板文件
		_elementTpl = ElementTemplet.getInstance().getElementMap(pageType);
		
		//解析页面中的参数
		page = parseParam(page, _param_regex);
		
		//解析控件元素
		page = parseElement(page, _element_regex);
		
		//解析信息返回前台
		page = page.trim();
		setReturnData(page);
		
		return _returnSuccess;
	}
	
	/**
	 * 解析控件的值
	 * @param name
	 * @return
	 */
	protected abstract String elementValue(String name);
	
	/**
	 * 解析参数的值
	 * @param name
	 * @return
	 */
	protected abstract String paramValue(String name);
	
	/**
	 * 解析字符串中的元素
	 * @param page
	 * @param regex
	 * @return
	 */
	protected String parseElement(String page, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(page);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String tag = m.group();
			String name = tagName(tag);
			//取元素的值
			String reptext = elementValue(name);
			m.appendReplacement(sb, reptext);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 解析字符串中的参数
	 * @param page
	 * @param regex
	 * @return
	 */
	protected String parseParam(String page, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(page);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String tag = m.group();
			String name = tagName(tag);
			//取参数的值
			String reptext = paramValue(name);
			m.appendReplacement(sb, reptext);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	/**
	 * 取功能的页面设计信息
	 * @param funcId
	 * @param pageType
	 * @return
	 */
	private String readDesignPage(String funcId, String pageType){
		String sql = "select page_content from fun_design " +
				"where fun_id = '"+ funcId +"' and page_type = '"+ pageType +"' ";
		
		return BigFieldUtil.readStream(sql, "page_content", DefineName.DESIGN_NAME);
	}
	
	/**
	 * 解析标签中的名字，如<element:page/>取page的值
	 * @param tag
	 * @return
	 */
	private String tagName(String tag) {
		if (tag == null || tag.length() == 0) {//"错误参数：标签为空！"
			_log.showWarn(JsMessage.getValue("pageparser.tagnull"));
			return "";
		}
		
		return tag.substring(tag.indexOf(":")+1, tag.length()-2);
	}
}
