/*
 * DmException.java 2010-12-17
 * 
 * Copyright 2010 Guangzhou Donghong Software Technology Inc.
 * Licensed under the www.jxstar.org
 */
package org.jxstar.dm;

import java.text.MessageFormat;

/**
 * 数据库配置异常。
 *
 * @author TonyTan
 * @version 1.0, 2010-12-17
 */
public class DmException extends Exception {
	private static final long serialVersionUID = 3106828241744138978L;

	/**
	 * @param message
	 */
	public DmException(String message) {
		super(message);
	}
	
	public DmException(String message, Object ... params) {
		super(MessageFormat.format(message, params));
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DmException(String message, Throwable cause) {
		super(message, cause);
	}
}
