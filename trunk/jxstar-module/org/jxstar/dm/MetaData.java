/*
 * MetaData.java 2010-12-23
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.util.List;
import java.util.Map;


import org.jxstar.dao.BaseDao;
import org.jxstar.dao.DaoParam;
import org.jxstar.dm.reverse.MetaDataUtil;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 数据库元数据读取类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class MetaData {
	//日志对象
	protected static Log _log = Log.getInstance();
	//dao对象
	protected static BaseDao _dao = BaseDao.getInstance();
	
	/**
	 * 查询所有表对象
	 * @param dsName -- 数据源名
	 * @param notExists -- 是否不包括配置表中的
	 * @return
	 * @throws DmException
	 */
	public List<Map<String,String>> getTableMeta(String dsName, boolean notExists) {
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select table_name, table_space, table_title from v_table_info ");
		if (notExists) {
			sbsel.append("where table_name not in (select table_name from dm_tablecfg)");
		}
		
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
		sbsel.append("select column_id, table_name, field_name, field_title, ");
		sbsel.append("data_type, data_size, data_scale, nullable, default_value ");
		sbsel.append("from v_column_info where table_name = ? ");
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		
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
		return MetaDataUtil.getKeyMeta(tableName, dsName);
	}
	
	/**
	 * 查询表的索引信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public List<Map<String,String>> getIndexMeta(String tableName, String dsName) {
		List<Map<String,String>> lsIndex = MetaDataUtil.getIndexInfo(tableName, dsName);
		if (lsIndex.isEmpty()) return lsIndex;
		
		return getIndexMeta(lsIndex);
	}
	
	/**
	 * 构建表的索引信息
	 * @param lsIndex
	 * @return
	 */
	protected List<Map<String,String>> getIndexMeta(List<Map<String,String>> lsIndex) {
		List<Map<String,String>> lsNewIndex = FactoryUtil.newList();
		
		//组合索引字段信息
		String preIndex = "", preField = "", preUnique = "";
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
					mpNewIndex.put("isunique", preUnique);
					
					lsNewIndex.add(mpNewIndex);
				}
				
				preIndex = indexName;
				preField = indexField;
				preUnique = mpIndex.get("isunique");
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
	
	/**
	 * 数据库类型转换为配置类型
	 * @param dataType -- 数据类型
	 * @return
	 */
	public String getDataType(String dataType) {
		dataType = dataType.toLowerCase();
		
		if (dataType.equals("double") || dataType.indexOf("float") > -1 || 
				dataType.indexOf("num") > -1 || dataType.equals("decimal")) {
			return "number";
		} else if (dataType.indexOf("int") > -1) {
			return "int";
		} else if (dataType.equals("date") || dataType.indexOf("time") > -1) {
			return "date";
		} else if (dataType.equals("char")) {
			return "char"; 
		} else if (dataType.indexOf("varchar") > -1) {
			return "varchar"; 
		} else if (dataType.indexOf("lob") > -1) {
			return "blob"; 
		}
		
		return "varchar";
	}
}
