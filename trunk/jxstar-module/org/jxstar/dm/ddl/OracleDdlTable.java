/*
 * OracleDdlTable.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.ddl;

import org.jxstar.dm.DdlField;
import org.jxstar.dm.DdlIndex;
import org.jxstar.dm.DdlTable;

/**
 * ORACLE表对象管理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class OracleDdlTable extends DdlTable {
	/**
	 * 构建表对象
	 */
	public OracleDdlTable() {
		init();
	}
	
	/**
	 * 初始化全局对象
	 */
	public void init() {
		//创建ORACLE模板解析类
		_parser = new OracleDmParser();
		//创建字段解析对象
		_fieldObj = new DdlField(_parser);
		//创建索引解析对象
		_indexObj = new DdlIndex(_parser);
	}
	
}
