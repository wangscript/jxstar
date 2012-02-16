/*
 * TableConfig.java 2010-12-24
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.compare;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;

/**
 * 取数据库配置信息，用于compare包。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-24
 */
public class TableConfig {
	private static BaseDao _dao = BaseDao.getInstance();

	/**
	 * 取表配置的字段信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源
	 * @return
	 */
	public static List<Map<String,String>> getFieldCfg(String tableName, String dsName) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from dm_fieldcfg where table_id in ");
		sql.append("(select table_id from dm_table where table_name = ?) order by field_index");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		
		return _dao.query(param);
	}
	
	/**
	 * 取表配置中的主键字段名
	 * @param tableName -- 表名
	 * @param dsName -- 数据源
	 * @return
	 */
	public static String getKeyField (String tableName, String dsName) {
		StringBuilder sql = new StringBuilder();
		sql.append("select key_field from dm_tablecfg where table_name = ?");
		
		DaoParam param = _dao.createParam(sql.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		
		Map<String,String> mpField = _dao.queryMap(param);
		
		return mpField.get("key_field");
	}
	
	/**
	 * 根据表名取表配置ID
	 * @param tableName -- 表名
	 * @return
	 */
	public static String getTableId(String tableName) {
		String sql = "select * from dm_tablecfg where table_name = ?";
		
		DaoParam param = _dao.createParam(sql);
		param.addStringValue(tableName);
		
		Map<String,String> mpTable = _dao.queryMap(param);
		if (mpTable.isEmpty()) return "";
		
		return mpTable.get("table_id");
	}
}
