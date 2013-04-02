/*
 * DB2DdlTable.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.ddl;

import java.util.List;
import java.util.Map;

import org.jxstar.dm.DdlField;
import org.jxstar.dm.DdlIndex;
import org.jxstar.dm.DdlTable;
import org.jxstar.dm.DmException;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.util.MapUtil;

/**
 * db2表对象管理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class DB2DdlTable extends DdlTable {
	/**
	 * 构建表对象
	 */
	public DB2DdlTable() {
		init();
	}
	
	/**
	 * 初始化全局对象
	 */
	public void init() {
		//创建db2模板解析类
		_parser = new DB2DmParser();
		//创建字段解析对象
		_fieldObj = new DdlField(_parser);
		//创建索引解析对象
		_indexObj = new DdlIndex(_parser);
	}
	
	/**
	 * 处理DB2修改表格结构后执行重组表数据命令
	 */
	public List<String> getModifySql(String tableId) throws DmException {
		List<String> lssql = super.getModifySql(tableId);
		
		Map<String,String> mpTable = DmConfig.getTableCfg(tableId);
		String tableName = MapUtil.getValue(mpTable, "table_name");
		lssql.add("call sysproc.admin_cmd('reorg table "+ tableName +"');\r\n");
		
		return lssql;
	}
}
