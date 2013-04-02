/*
 * DmFactory.java 2010-12-21
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;


import org.jxstar.dao.util.DBTypeUtil;
import org.jxstar.dm.ddl.DB2DdlTable;
import org.jxstar.dm.ddl.MysqlDdlTable;
import org.jxstar.dm.ddl.OracleDdlTable;
import org.jxstar.dm.ddl.SqlServerDdlTable;
import org.jxstar.dm.reverse.DB2MetaData;
import org.jxstar.dm.reverse.OracleMetaData;

/**
 * 取数据DDL语句执行对象。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-21
 */
public class DmFactory {

	/**
	 * 取数据源对于的数据库的解析对象。
	 * @param dsName -- 数据源名，如果为空，则取缺省数据源
	 * @return
	 */
	public static DdlTable getDdlTable(String dsName) {
		String dbType = DBTypeUtil.getDbmsType(dsName);
		
		if (dbType.equals(DBTypeUtil.ORACLE)) {
			return new OracleDdlTable();
		} else if (dbType.equals(DBTypeUtil.MYSQL)) {
			return new MysqlDdlTable();
		} else if (dbType.equals(DBTypeUtil.SQLSERVER)) {
			return new SqlServerDdlTable();
		} else if (dbType.equals(DBTypeUtil.DB2)) {
			return new DB2DdlTable();
		};
		
		return null;
	}
	
	/**
	 * 取数据源对于的数据库元数据获取对象。
	 * @param dsName -- 数据源名，如果为空，则取缺省数据源
	 * @return
	 */
	public static MetaData getMetaData(String dsName) {
		String dbType = DBTypeUtil.getDbmsType(dsName);
		
		if (dbType.equals(DBTypeUtil.ORACLE)) {
			return new OracleMetaData();
		} else if (dbType.equals(DBTypeUtil.MYSQL)) {
			return new MetaData();
		} else if (dbType.equals(DBTypeUtil.SQLSERVER)) {
			return new MetaData();
		} else if (dbType.equals(DBTypeUtil.DB2)) {
			return new DB2MetaData();
		};
		
		return null;
	}
}
