/*
 * TransactionManagerImp.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.transaction;

import java.util.Map;

import org.jxstar.util.factory.FactoryUtil;

/**
 * 根据当前线程的hashCode来标示事务对象。
 * 
 * 采用下面的方法可以支持事务：
 * TransactionManager _tranMng = (TransactionManager) SystemFactory
 * 			.createSystemObject(SystemObject.getTransactionManagerName());
 * _tranMng.startTran();
 * try {
 *    ...
 *    执行业务的代码
 *    ...
 *    
 *    _tranMng.commitTran();
 * } catch(Exception e) {
 *    _tranMng.rollbackTran();
 * }
 * 执行业务的代码中的所有update操作都在一个事务当中。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class TransactionManagerImp implements TransactionManager {
	//保存所有的事务对象，key为事务ID
	private Map<String,Object> _mpTranObj = null;
	//日志对象
	//private Log _log = Log.getInstance();
	
	public TransactionManagerImp() {
		_mpTranObj = FactoryUtil.newMap();
	}

	/**
	 * 获取当前线程使用的事务对象，如果不存在，则自动创建一个事务对象。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionManager#getTransactionObject()
	 */
	public synchronized TransactionObject getTransactionObject() {
		String currID = getCurrentTransactionID();
		TransactionObject tranObj = (TransactionObject) _mpTranObj.get(currID);
		if (tranObj == null) {
			tranObj = new TransactionObjectImp();
			_mpTranObj.put(currID, tranObj);
		}
		
		return tranObj;
	}	
	
	/**
	 * 开始当前事务，设置当前事务对象为非自动提交属性。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionManager#startTran()
	 */
	public synchronized void startTran() {
		//_log.showDebug("TransactionManagerImp.startTran...");
		//取当前事务对象
		String currID = getCurrentTransactionID();
		TransactionObject tranObj = (TransactionObject) _mpTranObj.get(currID);
		if (tranObj == null) {
			tranObj = new TransactionObjectImp();
			_mpTranObj.put(currID, tranObj);
		}		
		//设置非自动提交，需要commitTran来提交
		tranObj.setAutoCommit(false);
	}	
	
	/**
	 * 当前事务提交。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionManager#commitTran()
	 */
	public synchronized void commitTran() throws TransactionException {
		//_log.showDebug("TransactionManagerImp.commitTran...");
		//取当前事务对象
		Object currID = getCurrentTransactionID();
		TransactionObject tranObj = (TransactionObject) _mpTranObj.get(currID);
		if (tranObj == null) {
			throw new TransactionException("commit use'transobject is null! ");
		}
		
		try {
			//提交
			tranObj.setAutoCommit(true);
			tranObj.commit();
		} finally {
			//删除当前事务对象
			_mpTranObj.remove(currID);
			tranObj = null;
		}
	}

	/** 
	 * 当前事务回滚。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionManager#rollbackTran()
	 */
	public synchronized void rollbackTran() throws TransactionException {
		//_log.showDebug("TransactionManagerImp.rollbackTran...");
		//取当前事务对象
		Object currID = getCurrentTransactionID();
		TransactionObject tranObj = (TransactionObject) _mpTranObj.get(currID);
		if (tranObj == null) {
			throw new TransactionException("rollback use'transobject is null! ");
		}
		
		try {
			//回滚
			tranObj.setAutoCommit(true);
			tranObj.rollback();
		} finally {
			//删除当前事务对象
			_mpTranObj.remove(currID);
			tranObj = null;
		}
	}
	
	/**
	 * 获取当前事务ID。
	 * 
	 * @see org.jxstar.dao.transaction.TransactionManager#getCurrentTransactionID()
	 */
	public synchronized String getCurrentTransactionID() {
		int currentThreadID = Thread.currentThread().hashCode();
		//_log.showDebug("TransactionManagerImp.currentThreadID=" + currentThreadID);
		return Integer.toString(currentThreadID);
	}
}
