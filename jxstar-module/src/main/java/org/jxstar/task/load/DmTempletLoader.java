/*
 * DesignTempletLoader.java 2009-10-31
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.task.load;


import org.jxstar.dm.util.DmTemplet;
import org.jxstar.task.SystemLoader;
import org.jxstar.util.resource.JsParam;

/**
 * 加载数据库模板文件的任务：
 * 1、模板文件存放路径是conf/dm/
 * 2、模板文件名的命名规则是：{数据库类型}-cfg.xml，如oracle-cfg.xml, mysql-cfg.xml
 *
 * @author TonyTan
 * @version 1.0, 2009-10-31
 */
public class DmTempletLoader extends SystemLoader {

	protected void load() {
		String realPath = _initParam.get(JsParam.REALPATH);
		String filePath = realPath + "conf/dm/";
		
		String fileName, logHead = "loaded database templet ";
		
		//支持的数据库类型
		String dbtype = _initParam.get("dbtype");
		String[] dbtypes = dbtype.split(",");
		
		DmTemplet temp = DmTemplet.getInstance();
		
		//加载各数据库类型的模板文件
		for (int i = 0, n = dbtypes.length; i < n; i++) {
			fileName = filePath+dbtypes[i]+"-cfg.xml";
			temp.read(fileName, dbtypes[i]);
			_log.showDebug(logHead + fileName);
		}

	}

}
