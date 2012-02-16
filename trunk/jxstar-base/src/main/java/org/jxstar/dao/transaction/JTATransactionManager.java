/*
 * JTATransactionManager.java 2008-4-4
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.transaction;

/**
 * 采用服务器事务框架, 待实现.
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-4
 */
public class JTATransactionManager implements TransactionManager {

	public void commitTran() throws TransactionException {
		// TODO Auto-generated method stub

	}

	public Object getCurrentTransactionID() {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionObject getTransactionObject() {
		// TODO Auto-generated method stub
		return null;
	}

	public void rollbackTran() throws TransactionException {
		// TODO Auto-generated method stub

	}

	public void startTran() {
		// TODO Auto-generated method stub

	}

}
