/*
 * DataSourceConfig.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.pool;

import org.jxstar.util.config.SystemConfig;


/**
 * 数据源配置对象。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class DataSourceConfig {
	//缺省数据源名
	private static final String DEFAULT_DSNAME = "default";
	//这种数据源类型表示取datasource.xml中配置的数据源
	public static final String DSTYPE_SELF = "self";
	//这种数据源类型表示取应用服务器中配置的数据源
	public static final String DSTYPE_APPLICATION = "context";
	
	//数据源类型
	private String dataSourceType = DSTYPE_SELF;	
	//数据源描述
	private String dataSourceDesc;
	//数据源名
	private String dataSourceName;
	//数据库名
	private String schemaName;
	//JNDI名称
	private String jndiName;	
	//数据源的驱动程序
	private String driverClass;
	//JDBC的URL路径
	private String jdbcUrl;
	//数据源用户名
	private String userName;
	//数据源密码
	private String passWord;
	//数据源的最大连接数
	private String maxConNum = "50";
	//获取连接的最大等待时间(ms)
	private String maxWaitTime = "5000";
	//获取连接的事务级别
	private String tranLevel = "TRANSACTION_READ_COMMITTED";
	//数据库类型
	private String dbmsType;
	//是否检查连接有效性
	private String validTest = "false";
	//检查连接有效性的SQL
	private String validQuery = "";
	//启用检查空闲连接的线程
	private String validIdle = "false";
	//是否捕获异常
	private boolean catchError = true;

	public String toString() {
		StringBuilder sbDBS = new StringBuilder("\n\t");
			sbDBS.append("dataSourceType:" + dataSourceType + "\n\t");		
			sbDBS.append("dataSourceName:" + dataSourceName + "\n\t");
			sbDBS.append("driverClass:" + driverClass + "\n\t");
			sbDBS.append("jdbcUrl:" + jdbcUrl + "\n\t");
			sbDBS.append("userName:" + userName + "\n\t");
			sbDBS.append("passWord:" + passWord + "\n\t");
			sbDBS.append("maxConNum:" + maxConNum + "\n\t");
			sbDBS.append("maxWaitTime:" + maxWaitTime + "\n\t");
			sbDBS.append("tranLevel:" + tranLevel + "\n\t");
			sbDBS.append("jndiName:" + jndiName + "\n\t");
			sbDBS.append("dbmsType:" + dbmsType + "\n\t");
			sbDBS.append("validIdle:" + validIdle + "\n\t");
			sbDBS.append("validTest:" + validTest + "\n\t");
			sbDBS.append("validQuery:" + validQuery);
		
		return sbDBS.toString();
	}
	
	/**
	 * 获取系统的缺省数据源名称
	 * @return
	 */
	public static String getDefaultName() {
		String dsName = SystemConfig.getConfigByKey("ServerConfigs", "dsname");
		if (dsName.length() == 0) dsName = DEFAULT_DSNAME;
		return dsName;
	}
	
	public String getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}	

	public String getDataSourceDesc() {
		return dataSourceDesc;
	}

	public void setDataSourceDesc(String dataSourceDesc) {
		this.dataSourceDesc = dataSourceDesc;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getMaxConNum() {
		return maxConNum;
	}

	public void setMaxConNum(String maxConNum) {
		this.maxConNum = maxConNum;
	}

	public String getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(String maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getTranLevel() {
		return tranLevel;
	}

	public void setTranLevel(String tranLevel) {
		this.tranLevel = tranLevel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getDbmsType() {
		return dbmsType;
	}

	public void setDbmsType(String dbmstype) {
		this.dbmsType = dbmstype;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getValidTest() {
		return validTest;
	}

	public void setValidTest(String validTest) {
		this.validTest = validTest;
	}

	public String getValidQuery() {
		return validQuery;
	}

	public void setValidQuery(String validQuery) {
		this.validQuery = validQuery;
	}
	
	public String getValidIdle() {
		return validIdle;
	}

	public void setValidIdle(String validIdle) {
		this.validIdle = validIdle;
	}
	
	public boolean isCatchError() {
		return catchError;
	}

	public void setCatchError(boolean catchError) {
		this.catchError = catchError;
	}
}
