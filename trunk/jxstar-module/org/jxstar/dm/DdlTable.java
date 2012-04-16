/*
 * DmParser.java 2010-12-18
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.util.List;
import java.util.Map;

import org.jxstar.dm.util.DmConfig;
import org.jxstar.dm.util.DmUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;
import org.jxstar.util.resource.JsMessage;

/**
 * 数据库表对象定义语句管理类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-18
 */
public abstract class DdlTable {
	//日志对象
	protected Log _log = Log.getInstance();
	//当前数据源
	protected String _dsname = null;
	//模板解析对象
	protected DmParser _parser = null;
	//字段解析对象
	protected DdlField _fieldObj = null;
	//索引解析对象
	protected DdlIndex _indexObj = null;
	
	/**
	 * 初始化全局对象
	 * @param dbtype
	 */
	protected abstract void init();
	
	/**
	 * 创建数据表对象
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public boolean create(String tableId) throws DmException {
		//取执行SQL
		List<String> lssql = getCreateSql(tableId);
		if (lssql.isEmpty()) {
			//"创建表对象的SQL为空！"
			throw new DmException(JsMessage.getValue("ddltable.sqlnull"));
		}
		
		//执行语句
		if (!DmUtil.executeSQL(lssql, _dsname)) {
			_log.showError("execute sql error: " + lssql.toString());
			//"执行创建数据表对象的SQL出错！"
			throw new DmException(JsMessage.getValue("ddltable.createsqlnull"));
		}
		
		return true;
	}
	
	/**
	 * 构建生成表对象的SQL
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
		//取表标题SQL
		lssql.add(buildTableTitle(mpTable));
		//取字段标题SQL
		lssql.addAll(_fieldObj.buildFieldTitles(mpTable));
		//取创建主键SQL
		lssql.add(_indexObj.buildKey(mpTable));
		//取创建索引SQL
		lssql.addAll(_indexObj.buildIndexs(mpTable));
		
		_log.showDebug("create table sql:" + lssql.toString());
		
		return lssql;
	}
	
	/**
	 * 修改表对象
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public boolean modify(String tableId) throws DmException {
		//取执行SQL
		List<String> lssql = getModifySql(tableId);
		if (lssql.isEmpty()) {
			//throw new DmException("修改表对象的SQL为空！");
			//如果修改的配置字段不涉及表结构，则会出现没有更新SQL，如果表说明、表分类等字段修改后不需要改表结构
			//"修改表对象的SQL为空！"
			_log.showDebug(JsMessage.getValue("ddltable.updatesqlnull"));
			return true;
		}
		
		//执行语句
		if (!DmUtil.executeSQL(lssql, _dsname)) {
			_log.showError("execute sql error: " + lssql.toString());
			//"执行修改数据表对象的SQL出错！"
			throw new DmException(JsMessage.getValue("ddltable.updatesqlerror"));
		}
		
		return true;
	}
	
	/**
	 * 取修改表对象的SQL
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public List<String> getModifySql(String tableId) throws DmException {
		//取表配置信息
		Map<String,String> mpTable = DmConfig.getTableCfg(tableId);
		if (mpTable.isEmpty()) {
			//"没有找到【{0}】表的配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.tablenull"), tableId);
		}
		
		//取数据源
		_dsname = mpTable.get("ds_name");
		
		//检查当前数据表在数据库中是否存在，必须用正式表中的数据，因为可能会修改表名
		Map<String,String> mpOldTable = DmConfig.getTableOldCfg(tableId);
		String tableName = mpOldTable.get("table_name");
		if (!DmUtil.existTable(tableName, _dsname)) {
			//"数据库中不存在【{0}】表，请删除配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.nohastable"), tableName);
		}

		List<String> lssql = FactoryUtil.newList();
		//表信息修改SQL
		lssql.addAll(compareTable(mpTable));
		//字段信息修改SQL
		lssql.addAll(_fieldObj.compareFields(mpTable));
		//索引信息修改SQL
		lssql.addAll(_indexObj.compareIndexs(mpTable));

		_log.showDebug("alert table sql:" + lssql.toString());
		
		return lssql;
	}
	
	/**
	 * 删除表对象
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public boolean delete(String tableId) throws DmException {
		//取删除语句
		List<String> lssql = getDeleteSql(tableId);
		if (lssql.isEmpty()) {
			//"删除表对象的SQL为空！"
			throw new DmException(JsMessage.getValue("ddltable.delsqlnull"));
		}
		
		//执行语句
		boolean bret = DmUtil.executeSQL(lssql, _dsname);
		if (!bret) {
			_log.showError("execute sql error: " + lssql.toString());
			//"执行删除数据表对象的SQL出错！"
			throw new DmException(JsMessage.getValue("ddltable.delsqlerror"));
		}
		
		return true;
	}
	
	/**
	 * 取删除表对象的sql
	 * @param tableId -- 表配置ID
	 * @return
	 * @throws DmException
	 */
	public List<String> getDeleteSql(String tableId) throws DmException {
		//取表配置信息
		Map<String,String> mpTable = DmConfig.getTableCfg(tableId);
		if (mpTable.isEmpty()) {
			//"没有找到【{0}】表的配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.tablenull"), tableId);
		}
		
		//检查当前数据表在数据库中是否存在
		_dsname = mpTable.get("ds_name");
		String tableName = mpTable.get("table_name");
		if (!DmUtil.existTable(tableName, _dsname)) {
			//"数据库中不存在【{0}】表，请删除配置信息！"
			throw new DmException(JsMessage.getValue("ddltable.nohastable"), tableName);
		}
		
		List<String> lssql = FactoryUtil.newList();
		
		//取删除语句
		String sql = _parser.parseTemplet("drop_table", mpTable);
		
		lssql.add(sql);
		
		_log.showDebug("drop table sql:" + lssql.toString());
		
		return lssql;
	}
	
	/**
	 * 构建表信息创建的SQL
	 * @param mpTable -- 当前表信息
	 * @return
	 * @throws DmException
	 */
	protected String buildTable(Map<String,String> mpTable) throws DmException {
		StringBuilder sbTable = new StringBuilder();
		
		//构建用的字段信息用参数传递
		String buildFields = _fieldObj.buildFields(mpTable);
		mpTable.put("table_fields", buildFields);
		
		sbTable.append(_parser.parseTemplet("create_table", mpTable));
		sbTable.append("\r\n");
		
		return sbTable.toString();
	}
	
	/**
	 * 构建表标题的SQL
	 * @param mpTable -- 当前表信息
	 * @return
	 * @throws DmException
	 */
	protected String buildTableTitle(Map<String,String> mpTable) throws DmException {
		StringBuilder sbTable = new StringBuilder();
		
		sbTable.append(_parser.parseTemplet("table_title", mpTable));
		
		return sbTable.toString();
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
			String sql = _parser.parseTemplet("table_title", mpTable);
			lssql.add(sql);
		}
		
		return lssql;
	}
}
