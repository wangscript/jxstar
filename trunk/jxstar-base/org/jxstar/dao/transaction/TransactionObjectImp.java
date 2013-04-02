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
import java.util.List;
import java.util.Map;

import org.jxstar.dao.pool.DataSourceConfig;
import org.jxstar.dao.pool.PooledConnection;
import org.jxstar.util.factory.FactoryUtil;

/**
 * 负责获取数据连接，管理连接的提交、回滚、消亡。
 * 多数据源提交时存在问题：如果中间一个提交失败，则已提交的不能回滚了，这种情况极少见。
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
		//不能添加isClosed判断，因为存在isClosed时表示执行不正常了，必须把错误暴露出来。
		if (conn == null) {
			conn = PooledConnection.getInstance().getConnection(dsName);
			if (conn == null) {
				throw new TransactionException("datasource ["+ dsName +"] get connection is null!");
			}
			_mpConnection.put(dsName, conn);
		}
		
		return conn;
	}	

	/**
	 * 当前线程的所有数据连接提交。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#commit()
	 */
	public void commit() throws TransactionException {
		//如果非自动提交，则退出
		if (!_isAutoCommit) return;
		
		//如果没有更新的连接，则不用提交事务
		if (_mpConnection == null || _mpConnection.isEmpty()) {
			return;
		}
		
		//临时保存已经提交的连接
		List<String> lsTmp = FactoryUtil.newList();
		//从连接map中取出所有的连接提交
		Iterator<String> itr = _mpConnection.keySet().iterator();
		try {
			while(itr.hasNext()) {
				String dsName = (String) itr.next();
				lsTmp.add(dsName);
				//_log.showDebug(".........commit dsname is:" + dsName);
				
				Connection con = (Connection) _mpConnection.get(dsName);
				if (con == null){
					throw new TransactionException("connmap'connection is null! ");
				}
				
				try {
					con.commit();
					con.close();
					con = null;
				} catch (SQLException e) {
					throw new TransactionException(
							"tranobject.commit:" + e.getMessage());
				}
			}
		} finally {
			for (String key : lsTmp) {
				_mpConnection.remove(key);
			}
			
			//如果提交时出现异常，还需要回滚其它连接的操作
			if (!_mpConnection.isEmpty()) {
				rollback();
			}
		}
	}

	/**
	 * 当前线程的所有数据连接回滚。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionObject#rollback()
	 */
	public void rollback() throws TransactionException {
		//如果非自动提交，则退出
		if (!_isAutoCommit) return;
		
		//如果没有更新的连接，则不用回滚事务
		if (_mpConnection == null || _mpConnection.isEmpty()) {
			return;
		}		
		
		//临时保存已经回滚的连接
		List<String> lsTmp = FactoryUtil.newList();
		//从连接map中取出所有的连接，回滚
		Iterator<String> itr = _mpConnection.keySet().iterator();
		try {
			while(itr.hasNext()) {
				String dsName = (String) itr.next();
				lsTmp.add(dsName);
				//_log.showDebug(".........rollback dsname is:" + dsName);
				
				Connection con = (Connection) _mpConnection.get(dsName);
				if (con == null){
					throw new TransactionException("connmap'connection is null! ");
				}
				
				try {
					con.rollback();
					con.close();
					con = null;
				} catch (SQLException e) {
					throw new TransactionException(
							"tranobject.rollback:" + e.getMessage());
				}
			}
		} finally {
			for (String key : lsTmp) {
				_mpConnection.remove(key);
			}
			
			//如果提交时出现异常，还需要回滚其它连接的操作
			if (!_mpConnection.isEmpty()) {
				rollback();
			}
		}
	}
}
