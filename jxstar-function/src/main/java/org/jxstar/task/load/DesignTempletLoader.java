/*
 * DesignTempletLoader.java 2009-10-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.task.load;


import org.jxstar.fun.design.templet.ElementTemplet;
import org.jxstar.fun.design.templet.PageTemplet;
import org.jxstar.task.SystemLoader;
import org.jxstar.util.resource.JsParam;

/**
 * 加载设计模板文件的任务。
 *
 * @author TonyTan
 * @version 1.0, 2009-10-31
 */
public class DesignTempletLoader extends SystemLoader {

	protected void load() {
		String realPath = _initParam.get(JsParam.REALPATH);
		String filePath = realPath + "conf/tpl/";
		
		String fileName = "";
		String logHead = "loaded design file templet ";
		
		PageTemplet page = PageTemplet.getInstance();
		fileName = filePath + "grid-page-tpl.txt";
		page.read(fileName, "grid");
		_log.showDebug(logHead + fileName);
		
		fileName = filePath+"form-page-tpl.txt";
		page.read(fileName, "form");
		_log.showDebug(logHead + fileName);
		
		ElementTemplet element = ElementTemplet.getInstance();
		fileName = filePath+"grid-element-tpl.xml";
		element.read(fileName, "grid");
		_log.showDebug(logHead + fileName);;
		
		fileName = filePath+"form-element-tpl.xml";
		element.read(fileName, "form");
		_log.showDebug(logHead + fileName);
	}

}
