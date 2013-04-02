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
 * 读取DB2数据库中的元数据。
 * 由于DatabaseMetaData元数据读取时有些字段读不到信息，如：注释、缺省值等信息，所以直接从系统表读取。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-23
 */
public class DB2MetaData extends MetaData {
	
	/**
	 * 查询表的主键信息
	 * @param tableName -- 表名
	 * @param dsName -- 数据源名
	 * @return
	 * @throws DmException
	 */
	public Map<String,String> getKeyMeta(String tableName, String dsName) {
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select name as key_name, ");
		sbsel.append("replace(lower(substr(colnames, 2)), '+', ',') as key_field ");
		sbsel.append("from sysibm.sysindexes where creator = (current user) and ");
		sbsel.append("uniquerule = 'P' and lower(tbname) = ? ");
		
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
		StringBuilder sbsel = new StringBuilder();
		sbsel.append("select name as index_name, ");
		sbsel.append("replace(lower(substr(colnames, 2)), '+', ',') as index_field, ");
		sbsel.append("case uniquerule when 'U' then '1' else '0' end as isunique ");
		sbsel.append("from sysibm.sysindexes where creator = (current user) and ");
		sbsel.append("(uniquerule = 'U' or uniquerule = 'D') and lower(tbname) = ? ");
		
		DaoParam param = _dao.createParam(sbsel.toString());
		param.setDsName(dsName);
		param.addStringValue(tableName);
		
		return _dao.query(param);
	}
}
