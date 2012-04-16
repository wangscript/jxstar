/*
 * MysqlDdlTable.java 2010-12-18
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
import org.jxstar.dm.util.DmUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.resource.JsMessage;

/**
 * Mysql表对象管理类。
 * 
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public class MysqlDdlTable extends DdlTable {
	/**
	 * 构建表对象
	 */
	public MysqlDdlTable() {
		init();
	}
	
	/**
	 * 初始化全局对象
	 */
	public void init() {
		//创建MYSQL模板解析类
		_parser = new MysqlDmParser();
		//创建字段解析对象
		_fieldObj = new MysqlDdlField(_parser);
		//创建索引解析对象
		_indexObj = new DdlIndex(_parser);
	}
	
	/**
	 * 构建生成表对象的SQL，MySQL数据库中不需要单独构建描述信息SQL
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public List<String> getCreateSql(String tableId) throws DmException {
		//取表配置信息
		Map<String,String> mpTable = DmConfig.getTableCfg(tableId);
		if (mpTable.isEmpty()) {
			//"没有找到【{0}】表的配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.tablenull"), tableId);
		}
		
		//检查当前数据表在数据库中是否存在
		_dsname = mpTable.get("ds_name");
		String tableName = mpTable.get("table_name");
		if (DmUtil.existTable(tableName, _dsname)) {
			//"数据库中已存在【{0}】表，请利用数据库反向功能生成后再修改！"
			throw new DmException(JsMessage.getValue("ddltable.hasdbtable"), tableName);
		}
		
		List<String> lssql = FactoryUtil.newList();
		//取创建表SQL
		lssql.add(buildTable(mpTable));
		//取创建主键SQL
		lssql.add(_indexObj.buildKey(mpTable));
		//取创建索引SQL
		lssql.addAll(_indexObj.buildIndexs(mpTable));
		
		_log.showDebug("create table sql:" + lssql.toString());
		
		return lssql;
	}
}
