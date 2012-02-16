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
import org.jxstar.util.factory.FactoryUtil;

/**
 * 读取ORACLE数据库中的元数据。
 * 由于DatabaseMetaData元数据读取时有些字段读不到信息，如：注释、缺省值等信息，所以直接从系统表读取。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class OracleMetaData extends MetaData {
	
	/**
	 * 查询所有表对象
	 * @param dsName -- 数据源名
	 * @param notExists -- 是否不包括配置表中的
	 * @return
	 * @throws DmException
	 */
	public List<Map<String,String>> getTableMeta(String dsName, boolean notExists) {
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select lower(a.table_name) as table_name, ");
		sbsel.append("lower(a.tablespace_name) as table_space, ");
		sbsel.append("b.comments as table_title ");
		sbsel.append("from user_tables a, user_tab_comments b ");
		sbsel.append("where a.table_name =  b.table_name ");
		if (notExists) {
			sbsel.append(" and a.table_name not in (select upper(table_name) from dm_tablecfg) ");
		}
		sbsel.append("order by a.table_name");
		_log.showDebug("------------getTableMeta sql=" + sbsel.toString());
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		
		return _dao.query(param);
	}
	
	/**
	 * 查询指定表的字段信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public List<Map<String,String>> getFieldMeta(String tableName, String dsName) {
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select (a.column_id*10) as column_id, lower(a.table_name) as table_name, ");
		sbsel.append("lower(a.column_name) as field_name, b.comments as field_title, ");
		sbsel.append("lower(a.data_type) as data_type, a.data_length as data_size, ");
		sbsel.append("nvl(a.data_scale, 0) as data_scale, decode(a.nullable, 'Y', '0', '1') as nullable, ");
		sbsel.append("a.data_default as default_value ");
		sbsel.append("from user_tab_cols a, user_col_comments b ");
		sbsel.append("where a.table_name = b.table_name and a.column_name = b.column_name ");
		sbsel.append("and a.table_name = ? ");
		sbsel.append("order by a.column_id ");
		_log.showDebug("------------getFieldMeta sql=" + sbsel.toString());
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName.toUpperCase());
		
		return _dao.query(param);
	}
	
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
		sbsel.append("and a.constraint_type = 'P' and a.table_name = ? ");
		_log.showDebug("------------getKeyMeta sql=" + sbsel.toString());
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName.toUpperCase());
		
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
		sbsel.append("and b.column_name > ' ' and a.table_name = ? ");
		if (keyName.length() > 0) {
			sbsel.append(" and a.index_name <> '"+ keyName +"' ");
		}
		sbsel.append("order by a.index_name, b.column_position ");
		_log.showDebug("------------getIndexMeta sql=" + sbsel.toString());
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName.toUpperCase());
		//取到所有索引信息
		List<Map<String,String>> lsIndex = _dao.query(param);
		if (lsIndex.isEmpty()) {
			return lsIndex;
		}
		
		List<Map<String,String>> lsNewIndex = FactoryUtil.newList();
		
		//组合索引字段信息
		String preIndex = "", preField = "";
		for (int i = 0, n = lsIndex.size(); i < n; i++) {
			Map<String,String> mpIndex = lsIndex.get(i);
			
			String indexName = mpIndex.get("index_name");
			String indexField = mpIndex.get("column_name");
			
			//如果当前索引名与上次的相同，说明该索引有多个字段
			if (indexName.equals(preIndex)) {
				preField += "," + indexField;
			} else {				
				if (i > 0) {
					Map<String,String> mpNewIndex = FactoryUtil.newMap();
					mpNewIndex.put("index_name", preIndex);
					mpNewIndex.put("index_field", preField);
					mpNewIndex.put("isunique", mpIndex.get("isunique"));
					
					lsNewIndex.add(mpNewIndex);
				}
				
				preIndex = indexName;
				preField = indexField;
			}
			
			//最后一条
			if (i == n-1) {
				Map<String,String> mpNewIndex = FactoryUtil.newMap();
				mpNewIndex.put("index_name", preIndex);
				mpNewIndex.put("index_field", preField);
				mpNewIndex.put("isunique", mpIndex.get("isunique"));
				
				lsNewIndex.add(mpNewIndex);
			}
		}
		
		return lsNewIndex;
	}
}
