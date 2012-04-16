/*
 * TransactionObject.java 2008-3-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.transaction;

import java.sql.Connection;


/**
 * 事务对象，负责获取数据连接，管理连接的提交、回滚、消亡。
 * 
 * @author TonyTan
 * @version 1.0, 2008-3-30
 */
public interface TransactionObject {

	/**
	 * 获取数据库连接，取缺省数据数据源的连接。
	 * @return
	 */
	public Connection getConnection() throws TransactionException;
	
	/**
	 * 获取数据库连接，指定数据数据源名。
	 * @param sDataSourceName -- 数据源名
	 * @return
	 */
	public Connection getConnection(String sDataSourceName) throws TransactionException;
	
	/**
	 * 更新提交，如果事务管理器没有提交，事务不会真实的提交。
	 */
	public void commit() throws TransactionException;
	
	/**
	 * 回滚，如果事务管理器没有回滚，事务不会真实的回滚。
	 */
	public void rollback() throws TransactionException;
	
	/**
	 * 设置是否自动提交，事务管理器提交或回滚事务时isAutoCommit为1，否则为0，
	 * 在事务对象提交或回滚时，如果isAutoCommit为0，则事务不回执行。
	 * @param isAutoCommit
	 */
	public void setAutoCommit(boolean isAutoCommit);

	/**
	 * 取是否自动提交设置值。
	 * @return boolean
	 */
	public boolean getAutoCommit();
}
