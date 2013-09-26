/*
 * PooledConnection.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.log.Log;

/**
 * 该对象是DHSDP框架对外提供Connection对象的唯一工具
 * 
 * 该对象能提供不同数据源的数据连接，
 * 通过DataSourceConfig.setDataSourceName设置数据源的名字，
 * 缺数据源的名字为"default"。
 * 
 * 该对象能提供不同事务级别的数据连接，
 * 通过DataSourceConfig.setTranLevel设置事务级别。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class PooledConnection {
	private static PooledConnection _pooledConnection = null;
	//取应用服务的上下文
	private static Context _context = null;
	//自带数据源表
	private static Map<String,Object> _myDataSourceMap = null;
	//日志工具
	private static Log _log = Log.getInstance();
	
	private PooledConnection() {
		try {
			_context = new InitialContext();
			_myDataSourceMap = FactoryUtil.newMap();
		} catch (NamingException e) {
			_log.showError(e);
		}
	}
	
	/**
	 * 采用单例模式。
	 * @return
	 */
	public static synchronized PooledConnection getInstance() {
		if (_pooledConnection == null) {
			_pooledConnection = new PooledConnection();
		}
		
		return _pooledConnection;
	}

	/**
	 * 获取缺省数据源的连接，数据源名为deault，
	 * 返回的链接可能存在null值，使用时需要判断。
	 * @return 
	 */
	public Connection getConnection() {
		return getConnection(DataSourceConfig.getDefaultName());
	}

	/**
	 * 获取指定数据源名的连接，返回的链接可能存在null值，使用时需要判断。
	 * 去掉了synchronized关键字，没有意义，在多线程多数据源时，
	 * 如果取某个数据源连接时间过长时，会严重影响取本地数据库连接的效率。
	 * 
	 * @param dsName -- 数据源名
	 * @return
	 */
	public Connection getConnection(String dsName) {
		//如果数据源名为空或default，则取系统设置的缺省数据源名
		if (dsName == null || dsName.length() == 0 || dsName.equals("default")) {
			dsName = DataSourceConfig.getDefaultName();
		}
		
		Connection conn = null;
		DataSourceConfig dsConfig = DataSourceConfigManager.getInstance()
										.getDataSourceConfig(dsName);
		if (dsConfig == null) {
			_log.showWarn("DataSourceConfig is null! ");
			return conn;
		}
		//long sTime = System.currentTimeMillis();
		
		//如果是采用应用服务器的数据源，则从Context中取连接
		if (dsConfig.getDataSourceType()
				.equals(DataSourceConfig.DSTYPE_APPLICATION)) {
			conn = getConnectionFromContext(dsConfig);
		} else {
		//如果采用自带的数据源，则采用Apache的数据连接池
			conn = getConnectionFromSelf(dsConfig);
		}
		//long eTime = System.currentTimeMillis();
		//_log.showDebug("getConnection() use time:" + (eTime - sTime));
		
		return conn;
	}
	
	/**
	 * 为实现动态数据源的效果，需要支持替换数据源的功能，但这个方法不安全，需要改进。
	 * @param dsName
	 */
	public synchronized void delConnection(String dsName) {
		if (_myDataSourceMap.containsKey(dsName)) {
			_myDataSourceMap.remove(dsName);
		}
	}
	
	/**
	 * 从自建连接池中取连接。
	 * 去掉了原来的queueConnect方法，在取不到连接时重复取5次，
	 * 没有意义，1次取不到，取5次也没有反而造成性能问题；
	 * 可以设置validTest、validQuery、validIdle解决问题。
	 * 
	 * @param DataSourceConfig	-- 数据源配置对象
	 * @return
	 */	
	private Connection getConnectionFromSelf(DataSourceConfig dsConfig) {
		Connection conn = null;
		DataSource ds = createSelfDataSource(dsConfig);
		boolean catchError = dsConfig.isCatchError();
		
		try {
			conn = ds.getConnection();
			//_log.showDebug("get connection is:" + conn);
			
			if (conn == null || conn.isClosed()) {
				String dsName = dsConfig.getDataSourceName();
				_log.showError("datasource [{0}] get connection is null!", dsName);
				return null;
			}
			
			int iTranLevel = getTranLevelConstant(dsConfig.getTranLevel());
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(iTranLevel);
		} catch (SQLException e) {
			if (catchError) {
				_log.showError(e);
			}
		}
		
		return conn;
	}
	
	/**
	 * 取自带数据源
	 * @param dsName
	 * @return
	 */
	private DataSource createSelfDataSource(DataSourceConfig dsConfig) {
		String dsName = dsConfig.getDataSourceName();
		BasicDataSource ds = (BasicDataSource) _myDataSourceMap.get(dsName);
		if (ds != null) return ds;
		
		ds = new BasicDataSource();
		//取数据源设置的事务级别
		int iTranLevel = getTranLevelConstant(dsConfig.getTranLevel());
		int maxnum = Integer.parseInt(dsConfig.getMaxConNum());
		
		ds.setDriverClassName(dsConfig.getDriverClass());
		ds.setUrl(dsConfig.getJdbcUrl());
		ds.setUsername(dsConfig.getUserName());
		ds.setPassword(dsConfig.getPassWord());

		ds.setMaxIdle(maxnum);
		ds.setMaxActive(maxnum);
		ds.setMaxWait(Long.parseLong(dsConfig.getMaxWaitTime()));
		ds.setDefaultAutoCommit(false);
		ds.setDefaultTransactionIsolation(iTranLevel);
		
		//取缺省数据源时SystemVar还没有值，所以从server.xml中取值
		String validTest = dsConfig.getValidTest();
		String validQuery = dsConfig.getValidQuery();
		if (validTest.equalsIgnoreCase("true") && validQuery.length() > 0) {
			_log.showDebug("pool test use query...");
			ds.setTestOnBorrow(true);
			ds.setValidationQuery(validQuery);
			ds.setValidationQueryTimeout(3);
		}
		
		//启用线程检查，mysql在数据库端会过期关闭连接可以启用
		//开启此配置可以实现断开的连接自动恢复的效果
		if (dsConfig.getValidIdle().equalsIgnoreCase("true")) {
			_log.showDebug("pool idle valid thread started...");
			ds.setMinIdle(5);
			ds.setTestWhileIdle(true);
			
			//10分钟检查一次，空闲30分钟的连接被释放，保留5个空闲连接
			ds.setMinEvictableIdleTimeMillis(30*60*1000);//30 minus
			ds.setTimeBetweenEvictionRunsMillis(10*60*1000);//10 minus
		}
		
		//保存该数据源
		_myDataSourceMap.put(dsName, ds);
		
		return ds;
	}
	
	/**
	 * 从应用服务器的数据源中取连接。
	 * 
	 * @param dsName	-- 数据源名
	 * @param iTranLevel		-- 事务级别
	 * @return
	 */
	private Connection getConnectionFromContext(DataSourceConfig dsConfig) {
		Connection conn = null;
		String jndiName = dsConfig.getJndiName();
		boolean catchError = dsConfig.isCatchError();
		
		try {
			DataSource ds = (DataSource) _context.lookup(dsConfig.getJndiName());
			
			conn = ds.getConnection();
			//_log.showDebug("getConnection: " + conn);
			
			if (conn == null || conn.isClosed()) {
				String dsName = dsConfig.getDataSourceName();
				_log.showError("datasource [{0}] get connection is null!", dsName);
				return null;
			}
			
			int iTranLevel = getTranLevelConstant(dsConfig.getTranLevel());			
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(iTranLevel);
		} catch (NamingException e) {
			if (catchError) {
				_log.showError("error get jndi name is: " + jndiName);
				_log.showError(e);
			}
		} catch (SQLException e) {
			if (catchError) {
				_log.showError("error get jndi name is: " + jndiName);
				_log.showError(e);
			}
		}

		return conn;
	}
	
	/**
	 * 根据字符常量，取JDBC的事务级别
	 * Connection.TRANSACTION_NONE
	 * 		指示事务不受支持的常量。
	 * Connection.TRANSACTION_READ_UNCOMMITTED
	 * 		指示可以发生脏读、不可重复读和虚读的常量。
	 * Connection.TRANSACTION_READ_COMMITTED(缺省值)
	 * 		指示防止发生脏读的常量；不可重复读和虚读有可能发生。 
	 * Connection.TRANSACTION_REPEATABLE_READ
	 * 		指示防止发生脏读和不可重复读的常量；虚读有可能发生。
	 * Connection.TRANSACTION_SERIALIZABLE
	 * 		指示防止发生脏读、不可重复读和虚读的常量。
	 * 
	 * @param sTranLevel
	 * @return
	 */
	private int getTranLevelConstant(String sTranLevel) {
		if (sTranLevel == null || sTranLevel.length() == 0) {
			return Connection.TRANSACTION_READ_COMMITTED;
		}
		
		String sTmpLevel = sTranLevel.toUpperCase(); 
		if (sTmpLevel.equals("TRANSACTION_NONE")) {
			return Connection.TRANSACTION_NONE;
		} else if (sTmpLevel.equals("TRANSACTION_READ_UNCOMMITTED")) {
			return Connection.TRANSACTION_READ_UNCOMMITTED;
		} else if (sTmpLevel.equals("TRANSACTION_READ_COMMITTED")) {
			return Connection.TRANSACTION_READ_COMMITTED;
		} else if (sTmpLevel.equals("TRANSACTION_REPEATABLE_READ")) {
			return Connection.TRANSACTION_REPEATABLE_READ;
		} else if (sTmpLevel.equals("TRANSACTION_SERIALIZABLE")) {
			return Connection.TRANSACTION_SERIALIZABLE;
		}
		
		return Connection.TRANSACTION_READ_COMMITTED;
	}
}
