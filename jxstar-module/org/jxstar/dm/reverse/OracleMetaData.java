/*
 * OracleMetaData.java 2010-12-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm.reverse;

import java.util.List;
import java.util.Map;

import org.jxstar.dao.DaoParam;
import org.jxstar.dm.DmException;
import org.jxstar.dm.MetaData;

/**
 * 读取ORACLE数据库中的元数据。
 * 由于DatabaseMetaData元数据读取时有些字段读不到信息，如：注释、缺省值等信息，所以直接从系统表读取。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class OracleMetaData extends MetaData {
	
	/**
	 * 查询表的主键信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public Map<String,String> getKeyMeta(String tableName, String dsName) {
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select a.constraint_name as key_name, lower(b.column_name) as key_field ");
		sbsel.append("from user_constraints a, user_cons_columns b ");
		sbsel.append("where a.constraint_name = b.constraint_name and a.table_name = b.table_name ");
		sbsel.append("and a.constraint_type = 'P' and lower(a.table_name) = ? ");
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		
		return _dao.queryMap(param);
	}
	
	/**
	 * 查询表的索引信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public List<Map<String,String>> getIndexMeta(String tableName, String dsName) {
		//取主键索引名，处理索引中不包括主键索引
		String keyName = "";
		Map<String,String> mpKey = getKeyMeta(tableName, dsName);
		if (!mpKey.isEmpty()) {
			keyName = mpKey.get("key_name");
		}
		
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select a.index_name, decode(a.uniqueness, 'UNIQUE', '1', '0') as isunique, ");
		sbsel.append("lower(b.column_name) as column_name, b.column_position ");
		sbsel.append("from user_indexes a, user_ind_columns b ");
		sbsel.append("where a.table_name = b.table_name and a.index_name = b.index_name ");
		sbsel.append("and b.column_name > ' ' and lower(a.table_name) = ? ");
		if (keyName.length() > 0) {
			sbsel.append(" and a.index_name <> '"+ keyName +"' ");
		}
		sbsel.append("order by a.index_name, b.column_position ");
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		//取到所有索引信息
		List<Map<String,String>> lsIndex = _dao.query(param);
		if (lsIndex.isEmpty()) {
			return lsIndex;
		}
		
		return getIndexMeta(lsIndex);
	}
}
