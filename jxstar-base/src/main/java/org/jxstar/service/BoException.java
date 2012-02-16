/*
 * BoException.java 2009-5-28
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.service;

import java.text.MessageFormat;

/**
 * 业务处理异常对象。
 * 
 * @author TonyTan
 * @version 1.0, 2009-5-28
 */
public class BoException extends Exception {
	private static final long serialVersionUID = 2451599531477765055L;

	/**
	 * @param message
	 */
	public BoException(String message) {
		super(message);
	}
	
	public BoException(String message, Object ... params) {
		super(MessageFormat.format(message, params));
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BoException(String message, Throwable cause) {
		super(message, cause);
	}
}
