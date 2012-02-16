/*
 * TransactionObjectImp.java 2008-3-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;


import org.jxstar.dao.pool.DataSourceConfig;
import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 负责获取数据连接，管理连接的提交、回滚、消亡。
 * 
 * @author TonyTan
 * @version 1.0, 2008-3-30
 */
public class TransactionObjectImp implements TransactionObject {	
	//保存当前线程的给数据源的数据连接
	private Map<String,Object> _mpConnection = null;
	//数据连接是否自动提交
	private boolean _isAutoCommit = true;
	//日志对象
	//private Log _log = Log.getInstance();
	
	public TransactionObjectImp() {
		_mpConnection = FactoryUtil.newMap();
	}
	
	/**
	 * 读取数据连接是否自动提交。
	 * @return boolean
	 */
	public boolean getAutoCommit() {
		return _isAutoCommit;
	}

	/**
	 * 设置数据连接是否自动提交，
	 * 如果为false，则update后未提交事务，在事务管理器中提交。
	 * 
	 * @param autoCommit
	 */
	public void setAutoCommit(boolean autoCommit) {
		_isAutoCommit = autoCommit;
	}
	
	/**
	 * 获取数据连接，缺省数据源。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#getConnection()
	 */
	public Connection getConnection() throws TransactionException {
		return getConnection(DataSourceConfig.getDefaultName());
	}

	/**
	 * 获取数据连接，指定数据源。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#getConnection(java.lang.String)
	 */
	public Connection getConnection(String dsName) throws TransactionException {
		Connection conn = (Connection) _mpConnection.get(dsName);
		if (conn == null) {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) {
				throw new TransactionException("datasource ["+ dsName +"] get connection is null!");
			}
			_mpConnection.put(dsName, conn);
		}
		//_log.showDebug("TransactionObjectImp.conn=" + conn.hashCode() + " dsname=" + dsName);
		return conn;
	}	

	/**
	 * 当前线程的所有数据连接提交。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#commit()
	 */
	public void commit() throws TransactionException {
		//_log.println("commit.isAutoCommit=" + _isAutoCommit);		
		//如果非自动提交，则退出
		if (!_isAutoCommit) return;
		
		//如果没有更新的连接，则不用提交事务
		if (_mpConnection == null || _mpConnection.isEmpty()) {
			return;
		}
		
		//从连接map中取出所有的连接，回滚
		Iterator<String> itr = _mpConnection.keySet().iterator();
		while(itr.hasNext()) {
			String dsName = (String) itr.next();
			Connection con = (Connection) _mpConnection.get(dsName);
			if (con == null){
				throw new TransactionException("connmap'connection is null! ");
			}
			//_log.println("commit.con=" + con.hashCode() + ";dataSourcee=" + dsName);
			
			try {
				con.commit();
				con.close();
				con = null;
			} catch (SQLException e) {
				//_mpConnection.remove(dsName);
				throw new TransactionException(
						"tranobject.commit:" + e.getMessage());
			}
			//_mpConnection.remove(dsName);
		}
		//清除所有对象
		_mpConnection.clear();
	}

	/**
	 * 当前线程的所有数据连接回滚。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#rollback()
	 */
	public void rollback() throws TransactionException {
		//_log.println("rollback.isAutoCommit=" + _isAutoCommit);
		//如果非自动提交，则退出
		if (!_isAutoCommit) return;
		
		//如果没有更新的连接，则不用回滚事务
		if (_mpConnection == null || _mpConnection.isEmpty()) {
			return;
		}		
		
		//从连接map中取出所有的连接，回滚
		Iterator<String> itr = _mpConnection.keySet().iterator();
		while(itr.hasNext()) {
			String dsName = (String) itr.next();
			Connection con = (Connection) _mpConnection.get(dsName);
			if (con == null){
				throw new TransactionException("connmap'connection is null! ");
			}
			//_log.println("rollback.con=" + con.hashCode() + ";dataSourcee=" + dsName);
			
			try {
				con.rollback();
				con.close();
				con = null;
			} catch (SQLException e) {
				//_mpConnection.remove(dsName);
				throw new TransactionException(
						"tranobject.rollback:" + e.getMessage());
			}
			
			//_mpConnection.remove(dsName);
		}
		//清除所有对象
		_mpConnection.clear();
	}

}
