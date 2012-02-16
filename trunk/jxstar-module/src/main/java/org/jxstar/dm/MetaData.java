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
import org.jxstar.util.log.Log;

/**
 * 数据库元数据读取类。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public abstract class MetaData {
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
	public abstract List<Map<String,String>> getTableMeta(String dsName, boolean notExists);
	
	/**
	 * 查询指定表的字段信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public abstract List<Map<String,String>> getFieldMeta(String tableName, String dsName);
	
	/**
	 * 查询表的主键信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public abstract Map<String,String> getKeyMeta(String tableName, String dsName);
	
	/**
	 * 查询表的索引信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public abstract List<Map<String,String>> getIndexMeta(String tableName, String dsName);
	
	/**
	 * 数据库类型转换为配置类型
	 * @param dataType -- 数据类型
	 * @return
	 */
	public String getDataType(String dataType) {
		
		if (dataType.equals("double") || dataType.equals("float") || 
				dataType.equals("number") || dataType.equals("decimal")) {
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
