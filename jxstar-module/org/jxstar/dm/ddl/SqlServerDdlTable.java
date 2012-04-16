/*
 * SqlServerDdlTable.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.ddl;

import java.util.List;
import java.util.Map;

import org.jxstar.dm.DdlIndex;
import org.jxstar.dm.DdlTable;
import org.jxstar.dm.DmException;
import org.jxstar.dm.util.DmConfig;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * SQLSERVER表对象管理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class SqlServerDdlTable extends DdlTable {
	/**
	 * 构建表对象
	 */
	public SqlServerDdlTable() {
		init();
	}
	
	/**
	 * 初始化全局对象
	 */
	public void init() {
		//创建模板解析类
		_parser = new SqlServerDmParser();
		//创建字段解析对象
		_fieldObj = new SqlServerDdlField(_parser);
		//创建索引解析对象
		_indexObj = new DdlIndex(_parser);
	}
	
	/**
	 * 构建表信息修改的SQL
	 * @param mpTable -- 当前表信息
	 * @return
	 * @throws DmException
	 */
	protected List<String> compareTable(Map<String,String> mpTable) throws DmException {
		List<String> lssql = FactoryUtil.newList();
		
		//取当前表配置
		String tableId = mpTable.get("table_id");
		String tableName = mpTable.get("table_name");
		String tableTitle = mpTable.get("table_title");
		
		//取表原配置信息
		Map<String,String> mpTableOld = DmConfig.getTableOldCfg(tableId);
		if (mpTableOld.isEmpty()) {
			//"原配置表中没有找到【{0}】表的配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.oldtablenull"), tableId);
		}
		
		String tableOldName = mpTableOld.get("table_name");
		//取修改表名的SQL
		if (!tableOldName.equalsIgnoreCase(tableName)) {
			mpTable.put("old_table_name", tableOldName);
			String sql = _parser.parseTemplet("alter_table_name", mpTable);
			lssql.add(sql);
		}
		
		String tableOldTitle = mpTableOld.get("table_title");
		//取修改表标题的SQL
		if (!tableOldTitle.equals(tableTitle)) {
			String sql = _parser.parseTemplet("alter_table_title", mpTable);
			lssql.add(sql);
		}
		
		return lssql;
	}
}
