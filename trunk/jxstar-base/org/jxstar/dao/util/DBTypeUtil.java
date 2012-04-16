/*
 * DBTypeUtil.java 2008-4-8
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

import org.jxstar.dao.pool.DataSourceConfig;
import org.jxstar.dao.pool.DataSourceConfigManager;

/**
 * 数据库类型工具类.
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-8
 */
public class DBTypeUtil {
	//数据库类型常量
	public static final String ORACLE = "oracle";
	public static final String MYSQL = "mysql";
	public static final String SQLSERVER = "sqlserver";
	public static final String DB2 = "db2";
	public static final String SYBASE = "sybase";
	public static final String INFORMIX = "informix";
	public static final String POSTGRESQL = "postgresql";

	/**
	 * 获取缺省数据源的数据库类型.
	 * 
	 * @return String
	 */
	public static String getDbmsType() {
		return getDbmsType(DataSourceConfig.getDefaultName());
	}
	
	/**
	 * 获取指定数据源的数据库类型.
	 * 
	 * @param sDataSource - 数据源名
	 * @return String
	 */
	public static String getDbmsType(String sDataSource) {
		if (sDataSource == null || sDataSource.length() == 0) {
			sDataSource = DataSourceConfig.getDefaultName();
		}
		
		//取数据源配置对象
		DataSourceConfig dsc = DataSourceConfigManager.getInstance().
					getDataSourceConfig(sDataSource);
		if (dsc == null) return "";
		
		return dsc.getDbmsType();
	}
}
