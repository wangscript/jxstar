/*
 * SQLParseException.java 2008-4-8
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dao.util;

/**
 * 解析SQL中自定义函数抛出的异常.
 * 
 * @author TonyTan
 * @version 1.0, 2008-4-8
 */
public class SQLParseException extends Exception {
	private static final long serialVersionUID = 2451599531477765055L;
	
	
	/**
	 * @param message
	 */
	public SQLParseException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SQLParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
