/*
 * TransactionException.java 2008-3-30
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.transaction;

/**
 * 事务异常.
 * 
 * @author TonyTan
 * @version 1.0, 2008-3-30
 */
public class TransactionException extends Exception {
	private static final long serialVersionUID = 2745929753891425031L;

	/**
	 * @param message
	 */
	public TransactionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
